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

@Slf4j
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER')")
    public ResponseEntity<ApiResponse<Sale>> createSale(@Valid @RequestBody SaleRequest request) {
        log.info("REST request to create sale for branch: {}", request.getBranchId());
        return ResponseEntity.ok(ApiResponse.success(saleService.createSale(request), "Sale recorded successfully"));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER')")
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
