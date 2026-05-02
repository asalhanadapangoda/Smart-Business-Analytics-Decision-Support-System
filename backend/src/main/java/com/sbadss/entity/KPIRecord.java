package com.sbadss.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "kpi_records", indexes = {
        @Index(name = "idx_kpi_branch_date", columnList = "branch_id, record_date")
})
public class KPIRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "total_revenue", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_expenses", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    @Column(name = "net_profit", nullable = false, precision = 19, scale = 2)
    private BigDecimal netProfit = BigDecimal.ZERO;

    @Column(name = "active_customers")
    private Integer activeCustomers = 0;

    @Column(name = "total_transactions")
    private Integer totalTransactions = 0;

    @Column(name = "sales_growth_rate", precision = 10, scale = 4)
    private BigDecimal salesGrowthRate = BigDecimal.ZERO;

    @Column(name = "profit_margin", precision = 10, scale = 4)
    private BigDecimal profitMargin = BigDecimal.ZERO;
}
