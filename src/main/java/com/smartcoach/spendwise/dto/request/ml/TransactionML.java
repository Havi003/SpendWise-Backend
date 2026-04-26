package com.smartcoach.spendwise.dto.request.ml;

import java.math.BigDecimal;


import lombok.Data;

@Data
public class TransactionML {

    private BigDecimal amount;
    private String category;
    private String type;
    private String transactionDate;
    
}
