package com.smartcoach.spendwise.components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import com.smartcoach.spendwise.domain.entity.Goal;
import com.smartcoach.spendwise.dto.request.Goal.CreateGoalRequest;
import com.smartcoach.spendwise.dto.response.Goal.GoalResponse;

public class GoalMapper {

    public static Goal toEntity(UUID userId, CreateGoalRequest req) {
        Goal g = new Goal();
        g.setUserId(userId);
        g.setName(req.getName());
        g.setTargetAmount(req.getTargetAmount());
        g.setCurrentAmount(
            req.getCurrentAmount() == null ? BigDecimal.ZERO : req.getCurrentAmount()
        );
        g.setTargetDate(req.getTargetDate());
        g.setStatus("active");
        return g;
    }

    public static GoalResponse toResponse(Goal g) {

        double percentage = 0;

        if (g.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = g.getCurrentAmount()
                .divide(g.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }

        return new GoalResponse(
            g.getId(),
            g.getName(),
            g.getTargetAmount(),
            g.getCurrentAmount(),
            percentage,
            g.getTargetDate(),
            g.getStatus()
        );
    }
}