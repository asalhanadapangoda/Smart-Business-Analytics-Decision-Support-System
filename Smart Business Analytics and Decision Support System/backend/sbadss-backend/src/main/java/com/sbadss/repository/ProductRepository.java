package com.sbadss.repository;

import com.sbadss.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByBranchId(Long branchId);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByBranchIdAndCategoryId(Long branchId, Long categoryId);
}
