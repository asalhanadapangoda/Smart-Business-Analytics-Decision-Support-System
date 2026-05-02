package com.sbadss.controller;

import com.sbadss.common.ApiResponse;
import com.sbadss.dto.SaleRequest;
import com.sbadss.entity.Sale;
import com.sbadss.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.sbadss.dto.SaleResponse;

@Slf4j
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {


    private final SaleService saleService;
    
    @GetMapping("/ping")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("SaleController is reachable");
    }

    @GetMapping

    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getAllSales(@RequestParam(required = false) Long branchId) {
        log.info("REST request to fetch all sales for branch: {}", branchId);
        return ResponseEntity.ok(ApiResponse.success(saleService.getAllSales(branchId), "Sales fetched successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Sale>> createSale(@Valid @RequestBody SaleRequest request) {
        log.info("REST request to create sale for branch: {}", request.getBranchId());
        return ResponseEntity.ok(ApiResponse.success(saleService.createSale(request), "Sale recorded successfully"));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Sale>> completeSale(@PathVariable Long id) {
        log.info("REST request to complete sale: {}", id);
        return ResponseEntity.ok(ApiResponse.success(saleService.completeSale(id), "Bill completed successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSale(@PathVariable Long id) {
        log.info("REST request to delete sale: {}", id);
        saleService.deleteSale(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sale deleted successfully"));
    }
}
