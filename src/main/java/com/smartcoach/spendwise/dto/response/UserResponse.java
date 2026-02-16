package com.smartcoach.spendwise.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String fullName;
    private String email;
    private UUID smsWebhookId;

}
