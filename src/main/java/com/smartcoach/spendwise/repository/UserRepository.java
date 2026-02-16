package com.smartcoach.spendwise.repository;

import java.util.UUID;

import com.smartcoach.spendwise.domain.entity.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<com.smartcoach.spendwise.domain.entity.User, UUID> {

    Mono<Boolean> existsByEmail(String email);

    Mono<com.smartcoach.spendwise.domain.entity.User> findByEmail(String email);

    Mono<User> findBySmsWebhookId(UUID smsWebhookId);
}
