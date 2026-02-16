package com.smartcoach.spendwise.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.smartcoach.spendwise.domain.entity.Transaction;

import reactor.core.publisher.Flux;

// This repository will handle database operations for the Transaction entity.

@Repository
public interface TransactionRepository extends ReactiveCrudRepository <Transaction, UUID> {

    Flux <Transaction> findAllByUserId (UUID userId);

}
