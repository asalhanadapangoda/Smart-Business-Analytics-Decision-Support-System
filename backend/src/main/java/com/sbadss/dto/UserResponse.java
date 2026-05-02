package com.sbadss.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String roleName;
    private Long branchId;
    private String branchName;
    private boolean active;
}
