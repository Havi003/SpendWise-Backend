package com.smartcoach.spendwise.domain.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Data
@Table(schema="spendwise" ,name="balances")
public class Balance {

    @Id
    private UUID id;

    private UUID userId;
    private String source; // MPESA, BANK, CASH
    private BigDecimal currentBalance;

    private OffsetDateTime updatedAt;
}