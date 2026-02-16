package com.smartcoach.spendwise.service;

import com.smartcoach.spendwise.domain.entity.Transaction;

import reactor.core.publisher.Mono;

public interface CategorizationService {

    Mono<Transaction> categorizeTransaction(Transaction transaction);
    
}
