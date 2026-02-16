package com.smartcoach.spendwise.service;

import java.util.UUID;

import com.smartcoach.spendwise.dto.request.SmsWebhookRequest;

import reactor.core.publisher.Mono;

public interface WebhookService {

    Mono <Void> processSmsTransaction (UUID webhookId, SmsWebhookRequest request);

}
