package com.sbadss.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BranchRequest {

    @NotBlank(message = "Branch name is required")
    private String name;

    private String branchCode;

    @NotBlank(message = "Location is required")
    private String location;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid contact number format")
    private String contactNumber;

    private java.math.BigDecimal taxRate;
}
