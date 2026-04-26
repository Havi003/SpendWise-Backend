package com.smartcoach.spendwise.dto.response.helperItems;

import lombok.Data;

@Data
public class SpendingTrend {

    private String month;
    private double spending;
    private double income;

}
