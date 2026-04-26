package com.smartcoach.spendwise.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.smartcoach.spendwise.domain.entity.Balance;

import reactor.core.publisher.Flux;

public interface BalanceRepository extends ReactiveCrudRepository<Balance, UUID> {

    Flux<Balance> findByUserIdAndSource(UUID userId, String source);
    Flux<Balance> findByUserId(UUID userId);


}