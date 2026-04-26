package com.smartcoach.spendwise.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.smartcoach.spendwise.components.BudgetMapper;
import com.smartcoach.spendwise.components.WsResponseMapper;
import com.smartcoach.spendwise.domain.entity.Budget;
import com.smartcoach.spendwise.domain.entity.User;
import com.smartcoach.spendwise.dto.request.Budget.CreateBudgetRequest;
import com.smartcoach.spendwise.dto.request.Budget.UpdateBudgetRequest;
import com.smartcoach.spendwise.dto.response.WsHeader;
import com.smartcoach.spendwise.dto.response.WsResponse;
import com.smartcoach.spendwise.dto.response.Budget.BudgetResponse;
import com.smartcoach.spendwise.dto.response.Budget.BudgetSummaryResponse;
import com.smartcoach.spendwise.exception.BusinessException;
import com.smartcoach.spendwise.repository.BudgetRepository;
import com.smartcoach.spendwise.repository.TransactionRepository;
import com.smartcoach.spendwise.service.BudgetService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService{

    private final WsResponseMapper wsResponseMapper;
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    
// 1. Method to create a budget    
@Override
public Mono<WsResponse<BudgetResponse>> createBudget(
        UUID userId,
        CreateBudgetRequest request) {

    if (request.getLimitAmount() == null ||
        request.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0) {

        return Mono.just(
            wsResponseMapper.error("400", "Limit amount must be greater than zero")
        );
    }

    Budget budget = BudgetMapper.toEntity(userId, request);
budget.setCategoryName(budget.getCategoryName().toUpperCase());

    return budgetRepository
        .save(budget)
        .map(saved ->
            wsResponseMapper.success(
                "Budget created successfully",
                BudgetMapper.toResponse(saved, BigDecimal.ZERO)
            )
        );

}


// 2. Method to get User budgets
@Override
public Mono<WsResponse<List<BudgetResponse>>> getUserBudgets(User user) {

    UUID userId = user.getId();

    return budgetRepository.findByUserIdAndActive(userId, true)
        .flatMap(budget -> {

            LocalDate now = LocalDate.now();

            LocalDate startDate;
            LocalDate endDate;

            if ("MONTHLY".equalsIgnoreCase(budget.getPeriod())) {
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
            } else {
                startDate = budget.getStartDate();
                endDate = budget.getEndDate() != null ? budget.getEndDate() : now;
            }

            return transactionRepository
                .sumAmountByUserAndCategoryAndPeriod(
                    userId,
                    budget.getCategoryName(),
                    startDate,
                    endDate
                )
                .defaultIfEmpty(BigDecimal.ZERO)
                .map(spent -> BudgetMapper.toResponse(budget, spent));
        })
        .collectList()
        .map(budgets -> 
            new WsResponse<>(
                new WsHeader("200", "Budgets retrieved successfully"),
                budgets
            )
        );
}

// 3. Method to update an existing budget

@Override
public Mono<WsResponse<BudgetResponse>> updateBudget(
        User user,
        UUID budgetId,
        UpdateBudgetRequest request) {

            

    return budgetRepository
        .findById(budgetId)

        .switchIfEmpty(Mono.error(new BusinessException("Budget not found")))

        .flatMap(budget -> {
            if (!budget.getUserId().equals(user.getId())) {
                return Mono.error(new BusinessException("Unauthorized"));
            }

            if (request.getLimitAmount() != null &&
                request.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return Mono.error(new BusinessException("Invalid limit amount"));
            }

            if (request.getLimitAmount() != null) {
                budget.setLimitAmount(request.getLimitAmount());
            }

            if (request.getEndDate() != null) {
                budget.setEndDate(request.getEndDate());
            }

            return budgetRepository.save(budget);
        })

        .flatMap(updated ->
            transactionRepository
                .sumAmountByUserAndCategoryAndPeriod(
                    user.getId(),
                    updated.getCategoryName().toUpperCase(),
                    updated.getStartDate(),
                    LocalDate.now()
                )
                .defaultIfEmpty(BigDecimal.ZERO)
                .map(spent ->
                    wsResponseMapper.success(
                        "Budget updated successfully",
                        BudgetMapper.toResponse(updated, spent)
                    )
                )
        );
}

// 4. Method to get Budget Health Summary
@Override
public Mono<WsResponse<BudgetSummaryResponse>> getBudgetHealthSummary(User user) {

    Mono<BigDecimal> totalBudgeted =
        budgetRepository
            .sumActiveBudgetLimitsByUser(user.getId())
            .defaultIfEmpty(BigDecimal.ZERO);

    Mono<BigDecimal> totalSpent =
        transactionRepository
            .sumAllUserTransactions(user.getId())
            .defaultIfEmpty(BigDecimal.ZERO)
            .map(BigDecimal::abs);

    return Mono.zip(totalBudgeted, totalSpent)

        .map(tuple -> {

            BigDecimal budgeted = tuple.getT1();
            BigDecimal spent = tuple.getT2();

            double percentage = 0.0;


        if (budgeted.compareTo(BigDecimal.ZERO) > 0) {
            percentage = spent
                .divide(budgeted, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }

 

            String status;
            if (percentage >= 90) status = "critical";
            else if (percentage >= 75) status = "warning";
            else status = "good";

            return new BudgetSummaryResponse(
                budgeted,
                spent,
                percentage,
                status
            );
        })

        .map(summary ->
            wsResponseMapper.success(
                "Budget summary calculated successfully",
                summary
            )
        );
}

// 5. Meethod to delete a budget

@Override
public Mono<WsResponse<Void>> deleteBudget(User user, UUID budgetId){
    return budgetRepository.findById(budgetId)
        .switchIfEmpty(Mono.error(new BusinessException("Budget not found")))
        .flatMap(budget -> {
            if (!budget.getUserId().equals(user.getId())) {
                return Mono.error(new BusinessException("Unauthorized"));
            }
            // Use .delete() instead of .save()
            return budgetRepository.delete(budget); 
        })
        .thenReturn(wsResponseMapper.success("Budget deleted successfully", null));
}
}
