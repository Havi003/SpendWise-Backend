package com.smartcoach.spendwise.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.smartcoach.spendwise.dto.request.Goal.AddFundsRequest;
import com.smartcoach.spendwise.dto.request.Goal.CreateGoalRequest;
import com.smartcoach.spendwise.dto.response.Goal.GoalResponse;
import com.smartcoach.spendwise.dto.response.WsResponse;
import com.smartcoach.spendwise.exception.BusinessException;
import com.smartcoach.spendwise.repository.UserRepository;
import com.smartcoach.spendwise.service.GoalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final UserRepository userRepository;

    @PostMapping
    public Mono<WsResponse<GoalResponse>> createGoal(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateGoalRequest request) {

        String email = jwt.getSubject();

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BusinessException("User not found")))
                .flatMap(user -> goalService.createGoal(user.getId(), request));
    }

    @GetMapping
    public Mono<WsResponse<List<GoalResponse>>> getGoals(@AuthenticationPrincipal Jwt jwt) {

        String email = jwt.getSubject();

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BusinessException("User not found")))
                .flatMap(goalService::getUserGoals);
    }

    @PutMapping("/{goalId}/add-funds")
    public Mono<WsResponse<GoalResponse>> addFunds(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID goalId,
            @Valid @RequestBody AddFundsRequest request) {

        String email = jwt.getSubject();

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BusinessException("User not found")))
                .flatMap(user -> goalService.addFunds(user, goalId, request));
    }

    @DeleteMapping("/{goalId}")
    public Mono<WsResponse<Void>> deleteGoal(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID goalId) {

        String email = jwt.getSubject();

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BusinessException("User not found")))
                .flatMap(user -> goalService.deleteGoal(user, goalId));
    }
}