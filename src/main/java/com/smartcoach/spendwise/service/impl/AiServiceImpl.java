package com.smartcoach.spendwise.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.smartcoach.spendwise.dto.request.ml.TransactionML;
import com.smartcoach.spendwise.dto.response.ForecastResponse;
import com.smartcoach.spendwise.repository.TransactionRepository;
import com.smartcoach.spendwise.service.AiService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final TransactionRepository transactionRepository;
    private final WebClient webClient;

    private final String FAST_API_URL = "http://localhost:8000/predict";

    @Override
public Mono<ForecastResponse> generateForecast(UUID userId) {

    return transactionRepository.findAllByUserId(userId)
            .map(tx -> {
                TransactionML dto = new TransactionML();
                dto.setAmount(tx.getAmount());
                dto.setCategory(tx.getCategory());
                dto.setType(tx.getType());
                dto.setTransactionDate(tx.getTransactionDate().toString());
                return dto;
            })
            .collectList()
            .doOnNext(list -> {
                System.out.println("=== PAYLOAD SENT TO FASTAPI ===");
                list.forEach(System.out::println);
            })
            .flatMap(cleanList ->
                    webClient.post()
                            .uri(FAST_API_URL)

                            // ✅ FIXED PAYLOAD STRUCTURE
                            .bodyValue(java.util.Map.of("transactions", cleanList))

                            .retrieve()

                            // ✅ Proper error handling
                            .onStatus(status -> status.isError(), response ->
                                    response.bodyToMono(String.class)
                                            .flatMap(errorBody -> {
                                                System.out.println("=== FASTAPI ERROR RESPONSE ===");
                                                System.out.println(errorBody);
                                                return Mono.error(new RuntimeException("FastAPI Error: " + errorBody));
                                            })
                            )

                            .bodyToMono(ForecastResponse.class)
            );
}
}