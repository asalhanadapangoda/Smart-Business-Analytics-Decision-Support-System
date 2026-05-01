package com.sbadss.repository;

import com.sbadss.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    Optional<Sale> findByInvoiceNumber(String invoiceNumber);
    List<Sale> findByBranchId(Long branchId);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.status = 'COMPLETED' AND s.createdAt >= :startDate AND s.createdAt <= :endDate AND (:branchId IS NULL OR s.branchId = :branchId)")
    BigDecimal calculateTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("branchId") Long branchId);

    @Query("SELECT FUNCTION('DATE', s.createdAt) as saleDate, SUM(s.totalAmount) FROM Sale s WHERE s.status = 'COMPLETED' AND s.createdAt >= :startDate AND s.createdAt <= :endDate AND (:branchId IS NULL OR s.branchId = :branchId) GROUP BY FUNCTION('DATE', s.createdAt) ORDER BY saleDate ASC")
    List<Object[]> getSalesTrends(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("branchId") Long branchId);
}
