package com.sbadss.service.impl;

import com.sbadss.dto.ExpenseRequest;
import com.sbadss.entity.Branch;
import com.sbadss.entity.Expense;
import com.sbadss.entity.ExpenseCategory;
import com.sbadss.entity.User;
import com.sbadss.repository.BranchRepository;
import com.sbadss.repository.ExpenseCategoryRepository;
import com.sbadss.repository.ExpenseRepository;
import com.sbadss.repository.UserRepository;
import com.sbadss.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    @Override
    public List<Expense> getExpensesByBranch(Long branchId) {
        return branchId != null ? expenseRepository.findByBranchId(branchId) : expenseRepository.findAll();
    }

    @Override
    @Transactional
    public Expense createExpense(ExpenseRequest request) {
        log.info("Recording expense for branch: {}", request.getBranchId());
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User recorder = userRepository.findByUsername(username).orElseThrow();

        ExpenseCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Expense category not found"));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Branch not found"));

        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCategory(category);
        expense.setBranch(branch);
        expense.setRecordedBy(recorder);

        return expenseRepository.save(expense);
    }
}
