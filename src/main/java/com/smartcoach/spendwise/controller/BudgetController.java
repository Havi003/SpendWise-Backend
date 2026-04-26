package com.smartcoach.spendwise.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcoach.spendwise.dto.request.Budget.CreateBudgetRequest;
import com.smartcoach.spendwise.dto.request.Budget.UpdateBudgetRequest;
import com.smartcoach.spendwise.dto.response.WsResponse;
import com.smartcoach.spendwise.dto.response.Budget.BudgetResponse;
import com.smartcoach.spendwise.dto.response.Budget.BudgetSummaryResponse;
import com.smartcoach.spendwise.exception.BusinessException;
import com.smartcoach.spendwise.repository.UserRepository;
import com.smartcoach.spendwise.service.BudgetService;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    @PostMapping
    public Mono<WsResponse<BudgetResponse>> createBudget(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateBudgetRequest request) {

        String email = jwt.getSubject();

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BusinessException("User not found")))
                .flatMap(user ->
                        budgetService.createBudget(user.getId(), request)
                );
    }

    @GetMapping
    public Mono<WsResponse<List<BudgetResponse>>> getUserBudgets(@AuthenticationPrincipal Jwt jwt ){

        String email = jwt.getSubject();

        return userRepository.findByEmail(email)
        .switchIfEmpty(Mono.error(new BusinessException("User not found")))
        .flatMap(user -> budgetService.getUserBudgets(user));

    }

@PutMapping("/{budgetId}")
public Mono<WsResponse<BudgetResponse>> updateBudget(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID budgetId,
        @Valid @RequestBody UpdateBudgetRequest request) {

    String email = jwt.getSubject();

    return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(new BusinessException("User not found")))
            .flatMap(user -> budgetService.updateBudget(user, budgetId, request));
}

@GetMapping("/health")
public Mono<WsResponse<BudgetSummaryResponse>> getBudgetHealth(@AuthenticationPrincipal Jwt jwt) {
    String email = jwt.getSubject();

    return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(new BusinessException("User not found")))
            .flatMap(user -> budgetService.getBudgetHealthSummary(user));
}

@DeleteMapping("/{budgetId}")
public Mono<WsResponse<Void>> deleteBudget(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID budgetId){

    String email = jwt.getSubject();

    return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(new BusinessException("User not foundd")))
            .flatMap(user -> budgetService.deleteBudget(user, budgetId));
}

}
