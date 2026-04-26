package com.smartcoach.spendwise.service;

import java.util.UUID;

import com.smartcoach.spendwise.domain.entity.Account;

import reactor.core.publisher.Mono;

public interface WalletService {

    Mono<Account> getOrCreateWallet(UUID userId, String type);

}
