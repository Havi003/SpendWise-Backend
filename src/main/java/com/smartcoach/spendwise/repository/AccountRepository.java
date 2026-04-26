package com.smartcoach.spendwise.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.smartcoach.spendwise.domain.entity.Account;
import com.smartcoach.spendwise.domain.entity.Transaction;
import com.smartcoach.spendwise.dto.response.WsResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


//  This repository will handle database operations for the Account entity and includes a method to find all
//  accounts belonging to a specific user.
@Repository
public interface AccountRepository  extends ReactiveCrudRepository <Account, UUID>{

    Flux <Account> findByUserId (UUID userId);

    Mono<Account> findByUserIdAndAccountType(UUID userId, String accountType);


    
} 
