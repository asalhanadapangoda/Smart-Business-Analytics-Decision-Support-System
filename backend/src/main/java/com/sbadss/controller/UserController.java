package com.sbadss.controller;

import com.sbadss.common.ApiResponse;
import com.sbadss.dto.UserResponse;
import com.sbadss.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("GET /api/v1/users");
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(), "Users fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("GET /api/v1/users/{}", id);
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id), "User fetched successfully"));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByBranch(@PathVariable Long branchId) {
        log.info("GET /api/v1/users/branch/{}", branchId);
        return ResponseEntity.ok(ApiResponse.success(userService.getUsersByBranch(branchId), "Users fetched successfully"));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id, @RequestParam String roleName) {
        log.info("PATCH /api/v1/users/{}/role -> {}", id, roleName);
        return ResponseEntity.ok(ApiResponse.success(userService.updateUserRole(id, roleName), "User role updated successfully"));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(@PathVariable Long id) {
        log.info("PATCH /api/v1/users/{}/toggle-status", id);
        return ResponseEntity.ok(ApiResponse.success(userService.toggleUserStatus(id), "User status updated successfully"));
    }
}
