package com.smartcoach.spendwise.dto.request.Goal;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGoalRequest {

    @NotBlank
    private String name;

    @NotNull
    private BigDecimal targetAmount;

    private BigDecimal currentAmount;
    private LocalDate targetDate;
}
