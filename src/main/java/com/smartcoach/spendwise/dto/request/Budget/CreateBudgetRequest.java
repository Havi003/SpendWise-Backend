package com.smartcoach.spendwise.dto.request.Budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class CreateBudgetRequest {


    private String categoryName;
    private String name;
    private BigDecimal limitAmount;
    private String period;
    private LocalDate startDate;

    private BigDecimal alertThresholdWarning;
    private BigDecimal alertThresholdCritical;
}
