package com.smartcoach.spendwise.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.smartcoach.spendwise.components.GoalMapper;
import com.smartcoach.spendwise.components.WsResponseMapper;
import com.smartcoach.spendwise.domain.entity.Goal;
import com.smartcoach.spendwise.domain.entity.User;
import com.smartcoach.spendwise.dto.request.Goal.AddFundsRequest;
import com.smartcoach.spendwise.dto.request.Goal.CreateGoalRequest;
import com.smartcoach.spendwise.dto.response.Goal.GoalResponse;
import com.smartcoach.spendwise.dto.response.WsResponse;
import com.smartcoach.spendwise.exception.BusinessException;
import com.smartcoach.spendwise.repository.GoalRepository;
import com.smartcoach.spendwise.service.GoalService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final WsResponseMapper wsResponseMapper;

    @Override
    public Mono<WsResponse<GoalResponse>> createGoal(UUID userId, CreateGoalRequest request) {

        Goal goal = GoalMapper.toEntity(userId, request);

        return goalRepository.save(goal)
                .map(saved ->
                        wsResponseMapper.success(
                                "Goal created successfully",
                                GoalMapper.toResponse(saved)
                        )
                );
    }

    @Override
    public Mono<WsResponse<List<GoalResponse>>> getUserGoals(User user) {

        return goalRepository.findByUserId(user.getId())
                .map(GoalMapper::toResponse)
                .collectList()
                .map(list -> wsResponseMapper.success("Goals fetched successfully", list));
    }

    @Override
    public Mono<WsResponse<GoalResponse>> addFunds(User user, UUID goalId, AddFundsRequest request) {

        return goalRepository.findById(goalId)
                .switchIfEmpty(Mono.error(new BusinessException("Goal not found")))

                .flatMap(goal -> {
                    if (!goal.getUserId().equals(user.getId())) {
                        return Mono.error(new BusinessException("Unauthorized"));
                    }

                    BigDecimal newAmount =
                            goal.getCurrentAmount().add(request.getAmount());

                    goal.setCurrentAmount(newAmount);

                    if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
                        goal.setStatus("completed");
                    }

                    return goalRepository.save(goal);
                })

                .map(updated ->
                        wsResponseMapper.success(
                                "Funds added successfully",
                                GoalMapper.toResponse(updated)
                        )
                );
    }

    @Override
    public Mono<WsResponse<Void>> deleteGoal(User user, UUID goalId) {

        return goalRepository.findById(goalId)
                .switchIfEmpty(Mono.error(new BusinessException("Goal not found")))
                .flatMap(goal -> {
                    if (!goal.getUserId().equals(user.getId())) {
                        return Mono.error(new BusinessException("Unauthorized"));
                    }
                    return goalRepository.delete(goal);
                })
                .thenReturn(wsResponseMapper.success("Goal deleted successfully", null));
    }
}