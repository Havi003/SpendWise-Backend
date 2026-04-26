package com.smartcoach.spendwise.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Data
@Table(schema ="spendwise", name = "goal")
public class Goal {

    @Id
    private UUID id;

    private UUID userId;
    private String name;

    private BigDecimal targetAmount;
    private BigDecimal currentAmount;

    private LocalDate targetDate;
    private String status;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}