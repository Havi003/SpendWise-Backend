package com.smartcoach.spendwise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import com.smartcoach.spendwise.dto.response.helperItems.CategoryBreakdown;
import com.smartcoach.spendwise.dto.response.helperItems.ForecastItem;
import com.smartcoach.spendwise.dto.response.helperItems.SpendingTrend;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ForecastResponse {

    private double expectedSpending;
    private double expectedIncome;
    private double expectedBalance;
    private double confidence;

    private List<SpendingTrend> spendingTrend;
    private List<CategoryBreakdown> categoryBreakdown;
    private List<ForecastItem> forecast;

    private List<String> recommendations;

    private List<Object> anomalies;
    private List<Object> recurringExpenses;
}