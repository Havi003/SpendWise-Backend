package com.smartcoach.spendwise.service;

import java.util.List;
import java.util.UUID;

import com.smartcoach.spendwise.domain.entity.Transaction;
import com.smartcoach.spendwise.dto.CategoryUpdateRequest;
import com.smartcoach.spendwise.dto.response.WsResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {

Mono<WsResponse<List<Transaction>>> getTransactions(UUID userId);
Mono<WsResponse<Transaction>> updateTransactionCategory(UUID userId, UUID transactionID, String category);
}
