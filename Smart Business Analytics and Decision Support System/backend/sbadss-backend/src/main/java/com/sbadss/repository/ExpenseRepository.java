package com.sbadss.repository;

import com.sbadss.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByBranchId(Long branchId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.expenseDate >= :startDate AND e.expenseDate <= :endDate AND (:branchId IS NULL OR e.branchId = :branchId)")
    BigDecimal calculateTotalExpenses(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("branchId") Long branchId);
}
