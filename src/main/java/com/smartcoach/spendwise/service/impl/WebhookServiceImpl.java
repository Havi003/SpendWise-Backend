package com.smartcoach.spendwise.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.smartcoach.spendwise.domain.entity.Transaction;
import com.smartcoach.spendwise.dto.request.SmsWebhookRequest;
import com.smartcoach.spendwise.exception.BusinessException;
import com.smartcoach.spendwise.repository.AccountRepository;
import com.smartcoach.spendwise.repository.TransactionRepository;
import com.smartcoach.spendwise.repository.UserRepository;
import com.smartcoach.spendwise.service.CategorizationService;
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

    @Override
    public Mono<Void> processSmsTransaction(UUID webhookId, SmsWebhookRequest request) {
        logger.info("Processing SMS webhook for webhookId: {}", webhookId);

        return userRepository.findBySmsWebhookId(webhookId)
            .switchIfEmpty(Mono.defer(() -> {
                logger.warn("User not found for webhookId: {}", webhookId);
                return Mono.error(new BusinessException("User not found for this webhook ID"));
            }))
            .flatMap(user -> accountRepository.findByUserId(user.getId()).next()
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("No account found for user: {}", user.getId());
                    return Mono.error(new BusinessException("No account found for this user"));
                }))
                .flatMap(account -> {
                    Transaction transaction = parseSmsMessage(request.getFrom(), request.getMessage());
                    transaction.setUserId(user.getId());
                    transaction.setAccountId(account.getId());
                    transaction.setSource("SMS");

                    return categorizationService.categorizeTransaction(transaction)
                        .flatMap(transactionRepository::save)
                        .doOnSuccess(saved -> logger.info("Saved transaction {} for user {}", saved.getId(), user.getId()));
                })
            )
            .then();
    }

    private Transaction parseSmsMessage(String from, String message) {
        logger.debug("Parsing SMS message from '{}': '{}'", from, message);

        BigDecimal amount = BigDecimal.ZERO;
        String type = "expense";
        String merchantName = "Unknown"; // Declared once at the top level
        String transactionCode = null;

        // 1. Unified Amount Parsing
        Pattern amountPattern = Pattern.compile("Ksh([\\d,]+\\.\\d{2})");
        Matcher amountMatcher = amountPattern.matcher(message);
        if (amountMatcher.find()) {
            try {
                amount = new BigDecimal(amountMatcher.group(1).replace(",", ""));
            } catch (NumberFormatException e) {
                logger.error("Error parsing amount from message: {}", message);
            }
        }

        // 2. M-PESA Specific Logic
        if ("MPESA".equalsIgnoreCase(from)) {
            // Determine Type
            if (message.toLowerCase().contains("received")) {
                type = "income";
            } else if (message.toLowerCase().contains("sent") || 
                       message.toLowerCase().contains("paid") || 
                       message.toLowerCase().contains("transferred")) {
                type = "expense";
            }

            // Fixed Merchant Regex: Captures "to", "from", or "transferred to/from"
            Pattern merchantPattern = Pattern.compile("(?:to|from|transferred to|transferred from)\\s+([A-Z0-9\\s\\-]+?)(?:\\s+on|\\s+account|\\s+balance|\\.)", Pattern.CASE_INSENSITIVE);
            Matcher merchantMatcher = merchantPattern.matcher(message);
            if (merchantMatcher.find()) {
                merchantName = merchantMatcher.group(1).trim(); // Re-assignment, no "String" prefix
            }

            // Transaction Code
            Pattern codePattern = Pattern.compile("([A-Z0-9]+)\\s+Confirmed");
            Matcher codeMatcher = codePattern.matcher(message);
            if (codeMatcher.find()) {
                transactionCode = codeMatcher.group(1);
            }
        }

        // 3. Build Transaction
        return Transaction.builder()
            .amount(type.equals("income") ? amount : amount.negate())
            .type(type)
            .description(message)
            .merchantName(merchantName)
            .transactionCode(transactionCode)
            .transactionDate(OffsetDateTime.now())
            .category("Uncategorized")
            .isManual(false) // Bug Fix: Always false initially; CategorizationService manages this
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();
    }
}