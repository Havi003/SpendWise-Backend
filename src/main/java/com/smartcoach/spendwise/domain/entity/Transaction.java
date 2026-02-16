package com.smartcoach.spendwise.domain.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(schema = "spendwise", name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    private UUID id;

    private UUID userId;

    private UUID accountId;

    private BigDecimal amount;

    private String type; // e.g., DEBIT, CREDIT

    private String category; // Assigned by Rule-Engine or User

    private String description;

    @Column("is_manual")
    private boolean isManual; // False if categorized by library, True if edited by user

    private String merchantName;

    private String transactionCode;
   
    private OffsetDateTime transactionDate;
   
    private String source; // e.g., M-PESA, BANK_SMS
   
    private OffsetDateTime createdAt;
   
    private OffsetDateTime updatedAt;
}