package com.smartcoach.spendwise.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BalanceResponse{
    private BigDecimal mpesa = BigDecimal.ZERO;
    private BigDecimal cash = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
}