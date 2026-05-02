package com.sbadss.controller;

import com.sbadss.common.ApiResponse;
import com.sbadss.dto.CustomerRequest;
import com.sbadss.dto.CustomerResponse;
import com.sbadss.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getCustomers(@RequestParam(required = false) Long branchId) {
        log.info("REST request to get customers for branch: {}", branchId);
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomersByBranch(branchId), "Customers fetched successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerRequest dto) {
        log.info("REST request to create customer: {}", dto.getName());
        return ResponseEntity.ok(ApiResponse.success(customerService.createCustomer(dto), "Customer created successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CustomerResponse>> searchByPhone(@RequestParam String phoneNumber) {
        log.info("REST request to search customer by phone: {}", phoneNumber);
        CustomerResponse customer = customerService.findByPhoneNumber(phoneNumber);
        if (customer == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "Customer not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(customer, "Customer found"));
    }
}
