package com.smartcoach.spendwise.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.smartcoach.spendwise.domain.entity.Account;
import com.smartcoach.spendwise.domain.entity.Transaction;
import com.smartcoach.spendwise.exception.BusinessException;
import com.smartcoach.spendwise.dto.request.ManualTransactionRequest;
import com.smartcoach.spendwise.dto.response.BalanceResponse;
import com.smartcoach.spendwise.dto.response.WsHeader;
import com.smartcoach.spendwise.dto.response.WsResponse;
import com.smartcoach.spendwise.repository.BalanceRepository;
import com.smartcoach.spendwise.repository.TransactionRepository;
import com.smartcoach.spendwise.repository.AccountRepository;
import com.smartcoach.spendwise.service.TransactionService;
import com.smartcoach.spendwise.service.WalletService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BalanceRepository balanceRepository;
    private final AccountRepository accountRepository;
    private final WalletService walletService;



    @Override
    public Mono<WsResponse<List<Transaction>>> getTransactions(UUID userId) {
        return transactionRepository.findAllByUserId(userId)
                .collectList()
                .map(list -> new WsResponse<>(
                        new WsHeader("SUCCESS", "Transactions retrieved successfully"), list));
    }

    @Override
    public Mono<WsResponse<Transaction>> updateTransactionCategory(UUID userId, UUID transactionID, String category) {
        return transactionRepository.findById(transactionID)
                .filter(tx -> tx.getUserId().equals(userId))
                .flatMap(tx -> {
                    tx.setCategory(category);
                    tx.setManual(true);
                    tx.setUpdatedAt(OffsetDateTime.now());
                    return transactionRepository.save(tx);
                })
                .map(updated -> new WsResponse<>(new WsHeader("200", "Success"), updated))
                .switchIfEmpty(Mono.just(new WsResponse<>(new WsHeader("404", "Transaction not found"), null)));
    }

    @Override
    public Mono<WsResponse<Transaction>> createManualTransaction(UUID userId, ManualTransactionRequest request) {

        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setAccountId(request.getAccountId());

        // ✅ Normalize type safely
        String type = request.getType() != null ? request.getType().toUpperCase() : "EXPENSE";

       
            BigDecimal amount = request.getAmount() != null
                    ? request.getAmount()
                    : BigDecimal.ZERO;

            // enforce correct sign based on type
            if ("EXPENSE".equalsIgnoreCase(type)) {
                amount = amount.abs().negate();
            } else {
                amount = amount.abs();
            }

        tx.setAmount(amount);
        tx.setType(type);
        tx.setCategory(request.getCategory());
        tx.setDescription(request.getDescription());
        tx.setSource("CASH");
        tx.setManual(true);
        tx.setTransactionDate(OffsetDateTime.now());
        tx.setCreatedAt(OffsetDateTime.now());

        return walletService.getOrCreateWallet(userId, "CASH")
            .flatMap(account -> {

              tx.setAccountId(account.getId());

                return transactionRepository.save(tx)
                    .map(saved -> new WsResponse<>(
                            new WsHeader("201", "Manual transaction created"),
                            saved
                    ));
                });
    }

    @Override
    public Mono<WsResponse<BalanceResponse>> getUserBalance(UUID userId) {

        return transactionRepository.findAllByUserIdOrderByTransactionDateDesc(userId)
                .collectList()
                .flatMap(list -> {

                    BigDecimal transactionDelta = list.stream()
                            .filter(tx -> "CASH".equalsIgnoreCase(tx.getSource()))
                            .map(this::applySign)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return accountRepository.findByUserId(userId).next()
                            .defaultIfEmpty(new Account())
                            .map(account -> {

                                BigDecimal startingBalance = account.getStartingBalance() != null
                                        ? account.getStartingBalance()
                                        : BigDecimal.ZERO;

                                BigDecimal cashBalance = startingBalance.add(transactionDelta);

                                // MPESA parsing (unchanged but safer fallback)
                                BigDecimal latestMpesa = list.stream()
                                        .filter(tx -> tx.getDescription() != null &&
                                                tx.getDescription().toLowerCase().contains("m-pesa balance"))
                                        .findFirst()
                                        .map(tx -> {
                                            try {
                                                String msg = tx.getDescription().toLowerCase();
                                                String numbers = msg.replaceAll("[^0-9.]", "");
                                                return numbers.isEmpty()
                                                        ? BigDecimal.ZERO
                                                        : new BigDecimal(numbers);
                                            } catch (Exception e) {
                                                return BigDecimal.ZERO;
                                            }
                                        })
                                        .orElse(BigDecimal.ZERO);

                                BalanceResponse balance = new BalanceResponse(
                                        latestMpesa,
                                        cashBalance,
                                        cashBalance.add(latestMpesa) // Total aggregated using only cash and mpesa
                                );

                                return new WsResponse<>(
                                        new WsHeader("200", "Balances retrieved successfully"),
                                        balance
                                );
                            });
                });
    }

    // ✅ CENTRALIZED SIGN LOGIC (VERY IMPORTANT)
    private BigDecimal applySign(Transaction tx) {
        if ("EXPENSE".equalsIgnoreCase(tx.getType())) {
            return tx.getAmount().negate(); // subtract
        }
        return tx.getAmount(); // income stays positive
    }

    @Override
    public Mono<WsResponse<Account>> setStartingBalance(UUID accountId, BigDecimal startingBalance) {

        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")))
                .flatMap(account -> {

                    account.setStartingBalance(startingBalance);
                    account.setUpdatedAt(OffsetDateTime.now());

                    return accountRepository.save(account);
                })
                .map(updated -> new WsResponse<>(
                        new WsHeader("200", "Starting balance updated successfully"),
                        updated
                ));
    }

    

    
}