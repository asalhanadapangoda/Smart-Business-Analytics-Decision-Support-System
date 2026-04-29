package com.sbadss.controller;

import com.sbadss.common.ApiResponse;
import com.sbadss.dto.CustomerDTO;
import com.sbadss.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getCustomers(@RequestParam(required = false) Long branchId) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomersByBranch(branchId), "Customers fetched successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<CustomerDTO>> createCustomer(@RequestBody CustomerDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(customerService.createCustomer(dto), "Customer created successfully"));
    }
}
