package com.sbadss.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRequest {

    @NotBlank(message = "Customer name is required")
    private String name;

    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String address;

    @NotNull(message = "Branch ID is required")
    private Long branchId;
}
