package com.sbadss.repository;

import com.sbadss.entity.SaleItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    @Query("SELECT p.name, SUM(si.quantity) as totalQty FROM SaleItem si JOIN si.product p JOIN si.sale s WHERE s.status = 'COMPLETED' AND (:branchId IS NULL OR s.branch.id = :branchId) GROUP BY p.id, p.name ORDER BY totalQty DESC")
    List<Object[]> findTopProducts(@Param("branchId") Long branchId, Pageable pageable);
}
