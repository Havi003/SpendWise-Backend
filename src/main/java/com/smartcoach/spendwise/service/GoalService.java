package com.smartcoach.spendwise.service;

import java.util.List;
import java.util.UUID;

import com.smartcoach.spendwise.domain.entity.User;
import com.smartcoach.spendwise.dto.request.Goal.AddFundsRequest;
import com.smartcoach.spendwise.dto.request.Goal.CreateGoalRequest;
import com.smartcoach.spendwise.dto.response.Goal.GoalResponse;
import com.smartcoach.spendwise.dto.response.WsResponse;

import reactor.core.publisher.Mono;

public interface GoalService {

    Mono<WsResponse<GoalResponse>> createGoal(UUID userId, CreateGoalRequest request);

    Mono<WsResponse<List<GoalResponse>>> getUserGoals(User user);

    Mono<WsResponse<GoalResponse>> addFunds(User user, UUID goalId, AddFundsRequest request);

    Mono<WsResponse<Void>> deleteGoal(User user, UUID goalId);
    
}