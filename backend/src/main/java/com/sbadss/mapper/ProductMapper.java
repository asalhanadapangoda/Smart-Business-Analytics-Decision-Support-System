package com.sbadss.mapper;

import com.sbadss.dto.ProductRequest;
import com.sbadss.dto.ProductResponse;
import com.sbadss.entity.Branch;
import com.sbadss.entity.Category;
import com.sbadss.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null) return null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .price(product.getPrice())
                .purchasePrice(product.getPurchasePrice())
                .stockQuantity(product.getStockQuantity())
                .minThreshold(product.getMinThreshold())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .branchId(product.getBranch().getId())
                .branchName(product.getBranch().getName())
                .build();
    }

    public Product toEntity(ProductRequest request, Category category, Branch branch) {
        if (request == null) return null;

        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setMinThreshold(request.getMinThreshold() != null ? request.getMinThreshold() : 5);
        product.setCategory(category);
        product.setBranch(branch);
        product.setActive(true);
        return product;
    }
}
