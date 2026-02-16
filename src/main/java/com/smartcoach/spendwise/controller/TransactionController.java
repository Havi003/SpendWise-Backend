package com.smartcoach.spendwise.controller;

import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcoach.spendwise.domain.entity.Transaction;
import com.smartcoach.spendwise.domain.entity.User;
import com.smartcoach.spendwise.dto.CategoryUpdateRequest;
import com.smartcoach.spendwise.dto.response.WsResponse;
import com.smartcoach.spendwise.exception.BusinessException;
import com.smartcoach.spendwise.repository.TransactionRepository;
import com.smartcoach.spendwise.repository.UserRepository;
import com.smartcoach.spendwise.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    /**
     * Updates the category of a transaction manually.
     * When a user performs this action, we set isManual to true.
     */
@PutMapping("/{id}/category")
public Mono<WsResponse<Transaction>> updateCategory(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id,
        @RequestBody CategoryUpdateRequest request) {
    
    // 1. Get email from subject (litunda@gmail.com)
    String email = jwt.getSubject();

    // 2. Look up the user entity to get the UUID
    return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(new BusinessException("User not found")))
            .flatMap(user -> {
                // 3. Hand the validated User ID and the STRING category to the service
                return transactionService.updateTransactionCategory(user.getId(), id, request.getCategory());
            });
}
    

    @GetMapping
   public Mono<WsResponse<List<Transaction>>> getTransactions(@AuthenticationPrincipal Jwt jwt) {
    // The controller does nothing but hand the ID to the service
   String email = jwt.getSubject();

    return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(new BusinessException("User not found")))
            .flatMap(user -> transactionService.getTransactions(user.getId()));

}
}