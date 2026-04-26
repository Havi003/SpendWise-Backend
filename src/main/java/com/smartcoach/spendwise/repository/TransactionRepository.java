package com.smartcoach.spendwise.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.smartcoach.spendwise.domain.entity.Transaction;
import com.smartcoach.spendwise.repository.projection.MonthlyFinancialProjection;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// This repository will handle database operations for the Transaction entity.

@Repository
public interface TransactionRepository extends ReactiveCrudRepository <Transaction, UUID> {

    Flux <Transaction> findAllByUserId (UUID userId);

      // Sum spending for a category within period
    @Query("""
        SELECT COALESCE(SUM(ABS(amount)), 0)
        FROM spendwise.transactions
        WHERE user_id = :userId
        AND UPPER(TRIM(category)) = UPPER(TRIM(:categoryName))
        AND DATE(transaction_date) BETWEEN :startDate AND :endDate
        AND UPPER(type) = 'EXPENSE'
    """)
    Mono<BigDecimal> sumAmountByUserAndCategoryAndPeriod(
        UUID userId,
        String categoryName,
        LocalDate startDate,
        LocalDate endDate
    );

    // Total spending for dashboard summary
    @Query("""
        SELECT COALESCE(SUM(amount), 0)
        FROM spendwise.transactions
        WHERE user_id = :userId
        AND UPPER(type) = 'EXPENSE'
    """)
    Mono<BigDecimal> sumAllUserTransactions(UUID userId);

    Flux<Transaction> findAllByUserIdOrderByTransactionDateDesc(UUID userId);

    @Query("""
    SELECT 
        DATE_TRUNC('month', transaction_date) AS month,
        SUM(CASE WHEN type = 'INCOM' THEN amount ELSE 0 END) AS totalIncome,
        SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS totalExpense,
        SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) -
        SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS netCashflow
    FROM spendwise.transactions
    WHERE user_id = :userId
    GROUP BY DATE_TRUNC('month', transaction_date)
    ORDER BY month
    """)
    Flux<MonthlyFinancialProjection> getMonthlyFinancials(UUID userId);
}
