package com.smartcoach.spendwise.domain.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(schema="spendwise" , name="users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private UUID id;

    private String fullName;

    private String email;

    
    private boolean onboarded;

    private String passwordHash;

    private String authProvider;

    private UUID smsWebhookId;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;


}