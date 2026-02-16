package com.smartcoach.spendwise.service.impl;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.smartcoach.spendwise.domain.entity.Transaction;
import com.smartcoach.spendwise.dto.CategoryUpdateRequest;
import com.smartcoach.spendwise.dto.request.TransactionRequest;
import com.smartcoach.spendwise.dto.response.WsHeader;
import com.smartcoach.spendwise.dto.response.WsResponse;
import com.smartcoach.spendwise.repository.TransactionRepository;
import com.smartcoach.spendwise.service.TransactionService;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService{

    private final TransactionRepository transactionRepository;

    @Override
    public Mono<WsResponse<List<Transaction>>> getTransactions (UUID userId){
        
        return transactionRepository.findAllByUserId(userId)
        .collectList()
        .map(list -> new WsResponse<>(
            new WsHeader("SUCCESS", "Transactions retrieved successfully"), list));
    }

@Override
public Mono<WsResponse<Transaction>> updateTransactionCategory(UUID userId, UUID transactionID, String category) {
    return transactionRepository.findById(transactionID)
        .filter(tx -> tx.getUserId().equals(userId)) // Ownership check
        .flatMap(tx -> {
            tx.setCategory(category); // No more request.getCategory() needed here
            tx.setManual(true);
            tx.setUpdatedAt(OffsetDateTime.now());
            return transactionRepository.save(tx);
        })
        .map(updated -> new WsResponse<>(new WsHeader("200", "Success"), updated))
        .switchIfEmpty(Mono.just(new WsResponse<>(new WsHeader("404", "Transaction not found"), null)));
}




}
