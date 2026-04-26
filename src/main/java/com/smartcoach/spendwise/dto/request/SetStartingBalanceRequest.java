package com.smartcoach.spendwise.dto.request;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SetStartingBalanceRequest {
    private BigDecimal startingBalance;
}