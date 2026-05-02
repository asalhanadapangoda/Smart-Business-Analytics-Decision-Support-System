package com.sbadss.dto;

import com.sbadss.entity.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {
    private Long id;
    private String invoiceNumber;
    private String customerName;
    private String cashierName;
    private BigDecimal totalAmount;
    private SaleStatus status;
    private LocalDateTime createdAt;
}
