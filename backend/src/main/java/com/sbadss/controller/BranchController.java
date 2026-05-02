package com.sbadss.controller;

import com.sbadss.common.ApiResponse;
import com.sbadss.dto.BranchRequest;
import com.sbadss.dto.BranchResponse;
import com.sbadss.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAllBranches() {
        log.info("GET /api/v1/branches");
        return ResponseEntity.ok(ApiResponse.success(branchService.getAllBranches(), "Branches fetched successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable Long id) {
        log.info("GET /api/v1/branches/{}", id);
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranchById(id), "Branch fetched successfully"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@Valid @RequestBody BranchRequest request) {
        log.info("POST /api/v1/branches - name: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(branchService.createBranch(request), "Branch created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable Long id, @Valid @RequestBody BranchRequest request) {
        log.info("PUT /api/v1/branches/{}", id);
        return ResponseEntity.ok(ApiResponse.success(branchService.updateBranch(id, request), "Branch updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateBranch(@PathVariable Long id) {
        log.info("DELETE /api/v1/branches/{}", id);
        branchService.deactivateBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deactivated successfully"));
    }
}
