package com.smartcoach.spendwise.dto.response.Budget;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BudgetSummaryResponse {

    private BigDecimal totalBudgeted;
    private BigDecimal totalSpent;
    private Double percentageUsed;
    private String overallStatus;
}
