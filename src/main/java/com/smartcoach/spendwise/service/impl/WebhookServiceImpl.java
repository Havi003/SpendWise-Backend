package com.smartcoach.spendwise.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.smartcoach.spendwise.components.WsResponseMapper;
import com.smartcoach.spendwise.domain.entity.Account;
import com.smartcoach.spendwise.domain.entity.Balance;
import com.smartcoach.spendwise.domain.entity.Transaction;
import com.smartcoach.spendwise.dto.request.SmsWebhookRequest;
import com.smartcoach.spendwise.exception.BusinessException;
import com.smartcoach.spendwise.repository.AccountRepository;
import com.smartcoach.spendwise.repository.BalanceRepository;
import com.smartcoach.spendwise.repository.TransactionRepository;
import com.smartcoach.spendwise.repository.UserRepository;
import com.smartcoach.spendwise.service.CategorizationService;
import com.smartcoach.spendwise.service.WalletService;
import com.smartcoach.spendwise.service.WebhookService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookServiceImpl.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategorizationService categorizationService;
    private final WalletService walletService;
    private final BalanceRepository balanceRepository;
    private final WsResponseMapper wsResponseMapper;

    @Override
    public Mono<Void> processSmsTransaction(UUID webhookId, SmsWebhookRequest request) {
        logger.info("Processing SMS webhook for webhookId: {}", webhookId);

        return userRepository.findBySmsWebhookId(webhookId)
            .switchIfEmpty(Mono.defer(() -> {
                logger.warn("User not found for webhookId: {}", webhookId);
                return Mono.error(new BusinessException("User not found for this webhook ID"));
            }))
            .flatMap(user -> walletService.getOrCreateWallet(user.getId(), "MPESA")
                .flatMap(account -> {

                    Transaction transaction = parseSmsMessage(request.getFrom(), request.getMessage());
                    transaction.setUserId(user.getId());
                    transaction.setAccountId(account.getId());
                    transaction.setSource("SMS");

                    BigDecimal mpesaBalance = extractMpesaBalance(request.getMessage());

                    return categorizationService.categorizeTransaction(transaction)
                        .flatMap(transactionRepository::save)
                        .flatMap(savedTx ->
                            upsertBalance(user.getId(), "MPESA", mpesaBalance)
                                .thenReturn(savedTx)
                        )
                        .doOnSuccess(saved -> logger.info(
                                "Saved transaction {} and updated balance for user {}",
                                saved.getId(),
                                user.getId()
                        ));
                })
            )
            .then();
    }

        private Transaction parseSmsMessage(String from, String message) {
        logger.debug("Parsing SMS message from '{}': '{}'", from, message);

        BigDecimal amount = BigDecimal.ZERO;
        String type = "expense";
        String merchantName = "Unknown";
        String transactionCode = null;

        // =========================
        // ✅ AMOUNT EXTRACTION
        // =========================
        Pattern amountPattern = Pattern.compile("Ksh([\\d,]+\\.\\d{2})");
        Matcher amountMatcher = amountPattern.matcher(message);

        if (amountMatcher.find()) {
            try {
                amount = new BigDecimal(amountMatcher.group(1).replace(",", ""));
            } catch (NumberFormatException e) {
                logger.error("Error parsing amount from message: {}", message);
            }
        }

        if ("MPESA".equalsIgnoreCase(from)) {

            String lower = message.toLowerCase();

            // =========================
            // ✅ TYPE DETECTION
            // =========================
            if (lower.contains("received")) {
                type = "income";
            } else if (
                lower.contains("sent") ||
                lower.contains("paid") ||
                lower.contains("transferred")
            ) {
                type = "expense";
            }

            // =========================
            // ✅ CLEAN MERCHANT NAME
            // =========================
            Pattern merchantPattern = Pattern.compile(
                "(?:to|from|transferred to|transferred from)\\s+(.+?)(?:\\s+on|\\s+account|\\s+balance|\\.)",
                Pattern.CASE_INSENSITIVE
            );

            Matcher merchantMatcher = merchantPattern.matcher(message);
            if (merchantMatcher.find()) {
                merchantName = merchantMatcher.group(1).trim();
            }

            // =========================
            // ✅ STRONG TRANSACTION CODE
            // =========================
            Pattern codePattern = Pattern.compile("\\b([A-Z0-9]{8,12})\\b(?=\\s+Confirmed)");
            Matcher codeMatcher = codePattern.matcher(message);

            if (codeMatcher.find()) {
                transactionCode = codeMatcher.group(1);
            }
        }

        // =========================
        // ✅ CLEAN DESCRIPTION (IMPORTANT FOR FRONTEND)
        // =========================
        String cleanDescription = extractCleanDescription(message);

        return Transaction.builder()
            .amount(type.equals("income") ? amount : amount.negate())
            .type(type)
            .description(cleanDescription)   // 🔥 FIXED
            .merchantName(merchantName)
            .transactionCode(transactionCode)
            .transactionDate(OffsetDateTime.now())
            .category("Uncategorized")
            .isManual(false)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();
    }

    private String extractCleanDescription(String message) {

    
        // RECEIVED MONEY FORMAT
   
        Pattern receivedPattern = Pattern.compile(
            "(Confirmed\\.You have received Ksh[\\d,]+\\.\\d{2} from [A-Z\\s]+)",
            Pattern.CASE_INSENSITIVE
        );

        Matcher receivedMatcher = receivedPattern.matcher(message);
        if (receivedMatcher.find()) {
            return receivedMatcher.group(1).trim();
        }


        //  SENT / TRANSFERRED FORMAT

        Pattern sentPattern = Pattern.compile(
            "(Confirmed\\.Ksh[\\d,]+\\.\\d{2}\\s+(?:sent to|paid to|transferred to)\\s+.+?)(?:\\son\\s|M-?PESA|\\.)",
            Pattern.CASE_INSENSITIVE
        );

        Matcher sentMatcher = sentPattern.matcher(message);
        if (sentMatcher.find()) {
            return sentMatcher.group(1).trim();
        }


        // FALLBACK
  
        return message;
    }

    private BigDecimal extractMpesaBalance(String message) {
        Pattern pattern = Pattern.compile("New M-PESA balance is Ksh([\\d,]+\\.\\d{2})");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return new BigDecimal(matcher.group(1).replace(",", ""));
        }
        return null;
    }

    private Mono<Void> upsertBalance(UUID userId, String source, BigDecimal newBalance) {
        if (newBalance == null) return Mono.empty();

        return balanceRepository.findByUserIdAndSource(userId, source)
            .flatMap(balance -> {
                balance.setCurrentBalance(newBalance);
                balance.setUpdatedAt(OffsetDateTime.now());
                return balanceRepository.save(balance);
            })
            .switchIfEmpty(Mono.defer(() -> {
                Balance balance = new Balance();
                balance.setUserId(userId);
                balance.setSource(source);
                balance.setCurrentBalance(newBalance);
                balance.setUpdatedAt(OffsetDateTime.now());
                return balanceRepository.save(balance);
            }))
            .then();
    }

    
}