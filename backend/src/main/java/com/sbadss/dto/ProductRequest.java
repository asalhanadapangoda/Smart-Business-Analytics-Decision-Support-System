package com.sbadss.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String sku;

    private String description;

    @NotNull(message = "Product price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal purchasePrice;

    @NotNull(message = "Initial stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private Integer minThreshold;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Branch ID is required")
    private Long branchId;
}
