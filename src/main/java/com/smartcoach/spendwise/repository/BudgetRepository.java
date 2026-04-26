package com.smartcoach.spendwise.repository;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.smartcoach.spendwise.domain.entity.Budget;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BudgetRepository
        extends ReactiveCrudRepository<Budget, UUID> {

    // Get all active budgets for a user
    Flux<Budget> findByUserIdAndActive(UUID userId, boolean active);

    // Find specific category budget for a user
    Mono<Budget> findByUserIdAndCategoryName(UUID userId, String categoryName);

    // Sum all active budget limits for summary
    @Query("""
        SELECT COALESCE(SUM(limit_amount), 0)
        FROM spendwise.budget
        WHERE user_id = :userId
        AND active = true
    """)
    Mono<BigDecimal> sumActiveBudgetLimitsByUser(UUID userId);
}
