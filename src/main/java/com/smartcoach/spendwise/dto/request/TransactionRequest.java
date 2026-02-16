package com.smartcoach.spendwise.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class TransactionRequest {

    private BigDecimal amount;
    private String type;
    private String description;
    private String merchantName;
    private UUID accountId;
    private String transactionCode;
}
