package com.smartcoach.spendwise.service;

import com.smartcoach.spendwise.dto.UserCreationResult;
import com.smartcoach.spendwise.dto.request.LoginRequest;
import com.smartcoach.spendwise.dto.request.UserRegistrationRequest;
import com.smartcoach.spendwise.dto.response.JwtResponse;
import com.smartcoach.spendwise.dto.response.LoginResponse;
import com.smartcoach.spendwise.dto.response.UserResponse;
import com.smartcoach.spendwise.dto.response.WsResponse;

import reactor.core.publisher.Mono;

public interface AuthService {

    //Registration contract
    Mono<WsResponse<UserResponse>> register(UserRegistrationRequest request);

    //Login Contract
    Mono <WsResponse <LoginResponse>> login (LoginRequest request);

    Mono<UserCreationResult> findOrCreateGoogleUser(String email, String fullName);

    Mono<WsResponse<UserResponse>> getUserByEmail(String email);






}
