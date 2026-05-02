package com.sbadss.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BranchResponse {
    private Long id;
    private String name;
    private String branchCode;
    private String location;
    private String contactNumber;
    private java.math.BigDecimal taxRate;
    private boolean active;
}
