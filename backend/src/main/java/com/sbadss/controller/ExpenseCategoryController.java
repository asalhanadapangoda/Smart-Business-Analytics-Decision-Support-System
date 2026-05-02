package com.sbadss.controller;

import com.sbadss.common.ApiResponse;
import com.sbadss.entity.ExpenseCategory;
import com.sbadss.repository.ExpenseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expense-categories")
@RequiredArgsConstructor
public class ExpenseCategoryController {

    private final ExpenseCategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ExpenseCategory>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.findAll(), "Expense categories fetched successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseCategory>> createCategory(@RequestBody ExpenseCategory category) {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.save(category), "Expense category created successfully"));
    }
}
