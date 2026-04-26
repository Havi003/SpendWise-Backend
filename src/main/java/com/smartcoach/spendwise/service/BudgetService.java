package com.smartcoach.spendwise.service;

import java.util.List;
import java.util.UUID;

import com.smartcoach.spendwise.domain.entity.User;
import com.smartcoach.spendwise.dto.request.Budget.CreateBudgetRequest;
import com.smartcoach.spendwise.dto.request.Budget.UpdateBudgetRequest;
import com.smartcoach.spendwise.dto.response.WsResponse;
import com.smartcoach.spendwise.dto.response.Budget.BudgetResponse;
import com.smartcoach.spendwise.dto.response.Budget.BudgetSummaryResponse;

import reactor.core.publisher.Mono;

public interface BudgetService {

    Mono <WsResponse<BudgetResponse>> createBudget (UUID userId, CreateBudgetRequest request );
    Mono<WsResponse<List<BudgetResponse>>> getUserBudgets(User user);
    Mono<WsResponse<BudgetResponse>> updateBudget (User user, UUID budgetId, UpdateBudgetRequest request);
    Mono<WsResponse<BudgetSummaryResponse>> getBudgetHealthSummary(User user);
    Mono<WsResponse<Void>> deleteBudget(User user, UUID budgetId);


}
