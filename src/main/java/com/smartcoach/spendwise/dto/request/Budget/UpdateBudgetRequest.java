package com.smartcoach.spendwise.dto.request.Budget;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class UpdateBudgetRequest {

    private String name;
    private BigDecimal limitAmount;
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;


    private Boolean active;

    private BigDecimal alertThresholdWarning;
    private BigDecimal alertThresholdCritical;

}
