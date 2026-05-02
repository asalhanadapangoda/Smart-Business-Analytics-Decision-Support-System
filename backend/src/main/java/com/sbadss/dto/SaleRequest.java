package com.sbadss.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleRequest {
    private Long customerId;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotEmpty(message = "Sale must have at least one item")
    @Valid
    private List<SaleItemRequest> items;

    private String paymentMethod;
}
