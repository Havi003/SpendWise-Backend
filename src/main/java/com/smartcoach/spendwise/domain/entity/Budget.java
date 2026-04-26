package com.smartcoach.spendwise.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Data
@Table(schema = "spendwise", name = "budget")
public class Budget implements Persistable<UUID> {

    @Id
    @Column("budget_id")
    private UUID budgetId;

    @Column("user_id")
    private UUID userId;

    @Column("category_name")
    private String categoryName;

    private String name;

    @Column("limit_amount")
    private BigDecimal limitAmount;

    private String period;

    @Column("start_date")
    private LocalDate startDate;

    @Column("end_date")
    private LocalDate endDate;

    private Boolean active;

    @Column("alert_threshold_warning")
    private BigDecimal alertThresholdWarning;

    @Column("alert_threshold_critical")
    private BigDecimal alertThresholdCritical;

    @Column("created_at")
    private OffsetDateTime createdAt;

    // ... existing fields ...

    @Transient
    private boolean isNew = false; // Default to true for new instances

    @Override
    public UUID getId() {
        return budgetId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    // Helper to change state if loading from DB (not strictly needed for just creating)
    public Budget setAsNew(boolean isNew) {
        this.isNew = isNew;
        return this;
    }
}
