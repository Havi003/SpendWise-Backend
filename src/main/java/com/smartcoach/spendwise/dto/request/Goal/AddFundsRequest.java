package com.smartcoach.spendwise.dto.request.Goal;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddFundsRequest {

    @NotNull
    private BigDecimal amount;
}