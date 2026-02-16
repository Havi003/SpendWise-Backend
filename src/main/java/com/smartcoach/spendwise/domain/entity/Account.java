package com.smartcoach.spendwise.domain.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(schema = "spendwise", name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    private UUID id;

    private UUID userId;

    private String accountName;

    private String accountType;

    private BigDecimal balance;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

}
