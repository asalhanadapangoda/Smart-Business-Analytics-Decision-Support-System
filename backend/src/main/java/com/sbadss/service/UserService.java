package com.sbadss.service;

import com.sbadss.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUserRole(Long id, String roleName);
    UserResponse toggleUserStatus(Long id);
    UserResponse updateUser(Long id, com.sbadss.dto.RegisterRequest request);
    void deleteUser(Long id);
    List<UserResponse> getUsersByBranch(Long branchId);
}
