package com.smartcoach.spendwise.dto.response.helperItems;

import lombok.Data;

@Data
public class ForecastItem {

    private String month;
    private double predicted_expense;
    private double predicted_income;
    private double balance;
}
