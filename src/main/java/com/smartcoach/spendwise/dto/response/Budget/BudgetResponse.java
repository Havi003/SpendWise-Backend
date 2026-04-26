package com.smartcoach.spendwise.dto.response.Budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BudgetResponse {

    private UUID budgetId;
    private UUID userId;
    private String categoryName;

    private String name;
    private BigDecimal limitAmount;
    private String period;

    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;

    private BigDecimal alertThresholdWarning;
    private BigDecimal alertThresholdCritical;

    private OffsetDateTime createdAt;

    // computed fields (not stored in DB)
    private BigDecimal spentAmount;
    private Double percentageUsed;
    private String status;
}
