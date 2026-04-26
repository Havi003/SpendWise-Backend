package com.smartcoach.spendwise.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.smartcoach.spendwise.domain.entity.Goal;

import reactor.core.publisher.Flux;

public interface GoalRepository extends ReactiveCrudRepository<Goal, UUID> {

    Flux<Goal> findByUserId(UUID userId);
}