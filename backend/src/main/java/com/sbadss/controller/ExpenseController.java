package com.sbadss.controller;

import com.sbadss.common.ApiResponse;
import com.sbadss.dto.ExpenseRequest;
import com.sbadss.entity.Expense;
import com.sbadss.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Expense>>> getExpenses(@RequestParam(required = false) Long branchId) {
        log.info("REST request to get expenses for branch: {}", branchId);
        return ResponseEntity.ok(ApiResponse.success(expenseService.getExpensesByBranch(branchId), "Expenses fetched successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Expense>> createExpense(@Valid @RequestBody ExpenseRequest request) {
        log.info("REST request to create expense for branch: {}", request.getBranchId());
        return ResponseEntity.ok(ApiResponse.success(expenseService.createExpense(request), "Expense recorded successfully"));
    }
}
