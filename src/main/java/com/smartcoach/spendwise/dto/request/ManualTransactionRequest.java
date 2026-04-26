package com.smartcoach.spendwise.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class ManualTransactionRequest {

    private BigDecimal amount;
    private String category;
    private String description;
    private UUID accountId;
    private String type;
    
}
