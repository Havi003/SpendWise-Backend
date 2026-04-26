package com.smartcoach.spendwise.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthlyFinancialProjection {

    LocalDate getMonth();

    BigDecimal getTotalIncome();

    BigDecimal getTotalExpense();

    BigDecimal getNetCashflow();
}
