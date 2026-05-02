package com.sbadss.controller;

import com.sbadss.common.ApiResponse;
import com.sbadss.dto.ProductRequest;
import com.sbadss.dto.ProductResponse;
import com.sbadss.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long categoryId
    ) {
        log.info("REST request to get all products for branch: {} and category: {}", branchId, categoryId);
        return ResponseEntity.ok(ApiResponse.success(productService.getAllProducts(branchId, categoryId), "Products fetched successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest dto) {
        log.info("REST request to create product: {}", dto.getName());
        return ResponseEntity.ok(ApiResponse.success(productService.createProduct(dto), "Product created successfully"));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        log.info("REST request to update stock for product: {} with quantity: {}", id, quantity);
        productService.updateStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(null, "Stock updated successfully"));
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> importProducts(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("branchId") Long branchId) {
        log.info("REST request to import products for branch: {}", branchId);
        productService.importProducts(file, branchId);
        return ResponseEntity.ok(ApiResponse.success(null, "Products imported successfully"));
    }
}
