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
@Table(name = "prediction_records", indexes = {
        @Index(name = "idx_prediction_branch", columnList = "branch_id"),
        @Index(name = "idx_prediction_date", columnList = "prediction_date")
})
public class PredictionRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "prediction_date", nullable = false)
    private LocalDate predictionDate;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "predicted_revenue", nullable = false, precision = 19, scale = 2)
    private BigDecimal predictedRevenue;

    @Column(name = "lower_bound", precision = 19, scale = 2)
    private BigDecimal lowerBound;

    @Column(name = "upper_bound", precision = 19, scale = 2)
    private BigDecimal upperBound;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "model_used")
    private String modelUsed;

    @Column(name = "horizon_days")
    private Integer horizonDays;

    @Column(name = "actual_revenue", precision = 19, scale = 2)
    private BigDecimal actualRevenue;
}
