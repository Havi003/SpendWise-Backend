package com.smartcoach.spendwise.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.smartcoach.spendwise.domain.entity.Account;
import com.smartcoach.spendwise.repository.AccountRepository;
import com.smartcoach.spendwise.service.WalletService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService{

    private final AccountRepository accountRepository;
    @Override
    public Mono<Account> getOrCreateWallet(UUID userId, String type){

        return accountRepository.findByUserIdAndAccountType(userId, type)
            .switchIfEmpty(Mono.defer(() -> {

                Account wallet = new Account();
                wallet.setUserId(userId);
                wallet.setAccountType(type);
                wallet.setAccountName(type + " Wallet");
                wallet.setCreatedAt(OffsetDateTime.now());

                if ("CASH".equalsIgnoreCase(type)) {
                    wallet.setStartingBalance(BigDecimal.ZERO);
                }

                return accountRepository.save(wallet);
            }));
    }

    }
