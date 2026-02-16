package com.smartcoach.spendwise.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartcoach.spendwise.config.security.JwtUtil;
import com.smartcoach.spendwise.domain.entity.Account;
import com.smartcoach.spendwise.domain.entity.User;
import com.smartcoach.spendwise.dto.UserCreationResult;
import com.smartcoach.spendwise.dto.request.LoginRequest;
import com.smartcoach.spendwise.dto.request.UserRegistrationRequest;
import com.smartcoach.spendwise.dto.response.LoginResponse;
import com.smartcoach.spendwise.dto.response.UserResponse;
import com.smartcoach.spendwise.dto.response.WsHeader;
import com.smartcoach.spendwise.dto.response.WsResponse;
import com.smartcoach.spendwise.exception.BusinessException;
import com.smartcoach.spendwise.repository.AccountRepository;
import com.smartcoach.spendwise.repository.UserRepository;
import com.smartcoach.spendwise.service.AuthService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;

    @Override
    public Mono<WsResponse<UserResponse>> register(UserRegistrationRequest request) {

        return userRepository.existsByEmail(request.getEmail())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new BusinessException("Email already registered"));
                }

                User user = User.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .authProvider("email")
                    .onboarded(false) // IMPORTANT
                    .smsWebhookId(UUID.randomUUID())
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

                return userRepository.save(user)
                    .flatMap(savedUser -> {

                        Account defaultAccount = Account.builder()
                            .userId(savedUser.getId())
                            .accountName("M-Pesa")
                            .accountType("MOBILE_MONEY")
                            .balance(BigDecimal.ZERO)
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();

                        return accountRepository.save(defaultAccount)
                            .thenReturn(savedUser);
                    });
            })
            .map(savedUser ->
                new WsResponse<>(
                    new WsHeader("201", "User registered successfully"),
                    new UserResponse(
                        savedUser.getId(),
                        savedUser.getFullName(),
                        savedUser.getEmail(),
                        savedUser.getSmsWebhookId()
                    )
                )
            );
    }

    @Override
    public Mono<WsResponse<LoginResponse>> login(LoginRequest request) {

        return userRepository.findByEmail(request.getEmail())
            .switchIfEmpty(Mono.error(new BusinessException("Invalid email or password")))
            .flatMap(user -> {

                if (!"email".equals(user.getAuthProvider())) {
                    return Mono.error(new BusinessException("Please login using Google"));
                }

                if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                    return Mono.error(new BusinessException("Invalid email or password"));
                }

                boolean onboarded = Boolean.TRUE.equals(user.isOnboarded());

                String token = jwtUtil.generateToken(
                        user.getEmail(),
                        user.getId(),
                        onboarded
                );

                return Mono.just(
                    new WsResponse<>(
                        new WsHeader("200", "Login successful"),
                        new LoginResponse(token, onboarded)
                    )
                );
            });
    }

    public Mono<UserCreationResult> findOrCreateGoogleUser(String email, String fullName) {

        return userRepository.findByEmail(email)
            .map(user -> new UserCreationResult(user, false))
            .switchIfEmpty(
                userRepository.save(
                    User.builder()
                        .email(email)
                        .fullName(fullName)
                        .authProvider("google")
                        .onboarded(false) // IMPORTANT
                        .smsWebhookId(UUID.randomUUID())
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()
                )
                .flatMap(savedUser -> {

                    Account defaultAccount = Account.builder()
                        .userId(savedUser.getId())
                        .accountName("Default Account")
                        .accountType("GENERAL")
                        .balance(BigDecimal.ZERO)
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build();

                    return accountRepository.save(defaultAccount)
                        .thenReturn(savedUser);
                })
                .map(user -> new UserCreationResult(user, true))
            );
    }

    @Override
    public Mono<WsResponse<UserResponse>> getUserByEmail(String email) {
        System.out.println("Searching for user with email: " + email); // Add this line
        return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(new BusinessException("User not found")))
            .map(user -> new WsResponse<>(
                new WsHeader("200", "User details fetched successfully"),
                new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getSmsWebhookId())
            ));
    }
}
