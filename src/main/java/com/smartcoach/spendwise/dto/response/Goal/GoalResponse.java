package com.smartcoach.spendwise.dto.response.Goal;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GoalResponse(
        UUID id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        double percentage,
        LocalDate targetDate,
        String status
) {}