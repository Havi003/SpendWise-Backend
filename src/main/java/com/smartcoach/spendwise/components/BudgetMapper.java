package com.smartcoach.spendwise.components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.smartcoach.spendwise.domain.entity.Budget;
import com.smartcoach.spendwise.domain.entity.User;
import com.smartcoach.spendwise.dto.request.Budget.CreateBudgetRequest;
import com.smartcoach.spendwise.dto.response.Budget.BudgetResponse;

public class BudgetMapper {

    public static Budget toEntity(UUID userId, CreateBudgetRequest request) {

        Budget budget = new Budget();

        budget.setBudgetId(UUID.randomUUID());
        budget.setUserId(userId);
        budget.setNew(true);
        budget.setCategoryName(request.getCategoryName());
        budget.setName(request.getName());
        budget.setLimitAmount(request.getLimitAmount());
        budget.setPeriod(request.getPeriod());
        budget.setStartDate(request.getStartDate());
        budget.setActive(true);

        budget.setAlertThresholdWarning(
            request.getAlertThresholdWarning() != null
                ? request.getAlertThresholdWarning()
                : BigDecimal.valueOf(0.75)
        );

        budget.setAlertThresholdCritical(
            request.getAlertThresholdCritical() != null
                ? request.getAlertThresholdCritical()
                : BigDecimal.valueOf(0.90)
        );

        budget.setCreatedAt(OffsetDateTime.now());

        return budget;
    }

 public static BudgetResponse toResponse(
        Budget budget,
        BigDecimal spentInput) {

    BigDecimal limit = budget.getLimitAmount() != null
        ? budget.getLimitAmount()
        : BigDecimal.ZERO;

    BigDecimal spent = spentInput != null
        ? spentInput
        : BigDecimal.ZERO;

    double percentage = 0.0;

    if (limit.compareTo(BigDecimal.ZERO) > 0) {
        percentage = spent
            .divide(limit, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }

    String status = "good";
    if (percentage >= 90) status = "critical";
    else if (percentage >= 75) status = "warning";

    return BudgetResponse.builder()
        .budgetId(budget.getBudgetId())
        .userId(budget.getUserId())
        .categoryName(budget.getCategoryName())
        .name(budget.getName())
        .limitAmount(limit)
        .period(budget.getPeriod())
        .startDate(budget.getStartDate())
        .active(budget.getActive())
        .alertThresholdWarning(budget.getAlertThresholdWarning())
        .alertThresholdCritical(budget.getAlertThresholdCritical())
        .createdAt(budget.getCreatedAt())
        .spentAmount(spent)           // ✅ FIXED
        .percentageUsed(percentage)   // ✅ FIXED
        .status(status)
        .build();
}
}
