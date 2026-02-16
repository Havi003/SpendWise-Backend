package com.smartcoach.spendwise.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.smartcoach.spendwise.dto.request.SmsWebhookRequest;
import com.smartcoach.spendwise.service.WebhookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping ("api/webhook")
@RequiredArgsConstructor

public class WebHookController {

    @Autowired
    private final WebhookService webhookService;

    @PostMapping (value = "/sms/{webhookId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> handleSmsWebhook(@PathVariable UUID webhookId, @Valid @RequestBody SmsWebhookRequest request){

        return webhookService.processSmsTransaction(webhookId, request);
    }



}
