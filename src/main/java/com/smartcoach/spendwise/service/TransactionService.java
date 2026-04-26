package com.smartcoach.spendwise.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.smartcoach.spendwise.domain.entity.Account;
import com.smartcoach.spendwise.domain.entity.Transaction;
import com.smartcoach.spendwise.dto.request.ManualTransactionRequest;
import com.smartcoach.spendwise.dto.response.BalanceResponse;
import com.smartcoach.spendwise.dto.response.WsResponse;

import reactor.core.publisher.Mono;

public interface TransactionService {

    Mono<WsResponse<List<Transaction>>> getTransactions(UUID userId);
    Mono<WsResponse<Transaction>> updateTransactionCategory(UUID userId, UUID transactionID, String category);
    Mono<WsResponse<Transaction>> createManualTransaction(UUID userId,ManualTransactionRequest request);
        // NEW: Get user balances
    Mono<WsResponse<BalanceResponse>> getUserBalance(UUID userId);
    Mono<WsResponse<Account>> setStartingBalance(UUID accountId, BigDecimal startingBalance);


}
