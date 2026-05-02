package com.sbadss.service;

import com.sbadss.dto.ProductRequest;
import com.sbadss.dto.ProductResponse;
import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts(Long branchId, Long categoryId);
    ProductResponse createProduct(ProductRequest dto);
    void updateStock(Long productId, Integer quantity);
    void importProducts(org.springframework.web.multipart.MultipartFile file, Long branchId);
}
