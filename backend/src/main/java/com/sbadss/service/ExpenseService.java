package com.sbadss.service;

import com.sbadss.dto.ExpenseRequest;
import com.sbadss.entity.Expense;
import java.util.List;

public interface ExpenseService {
    List<Expense> getExpensesByBranch(Long branchId);
    Expense createExpense(ExpenseRequest request);
}
