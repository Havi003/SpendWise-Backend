package com.smartcoach.spendwise.service;

import java.util.UUID;

import com.smartcoach.spendwise.dto.response.ForecastResponse;

import reactor.core.publisher.Mono;

public interface AiService {

    Mono<ForecastResponse> generateForecast(UUID userId);

}
