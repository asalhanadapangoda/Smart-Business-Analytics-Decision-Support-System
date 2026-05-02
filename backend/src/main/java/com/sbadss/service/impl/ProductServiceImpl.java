package com.sbadss.service.impl;

import com.sbadss.dto.ProductRequest;
import com.sbadss.dto.ProductResponse;
import com.sbadss.entity.Branch;
import com.sbadss.entity.Category;
import com.sbadss.entity.Product;
import com.sbadss.mapper.ProductMapper;
import com.sbadss.repository.BranchRepository;
import com.sbadss.repository.CategoryRepository;
import com.sbadss.repository.ProductRepository;
import com.sbadss.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BranchRepository branchRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductResponse> getAllProducts(Long branchId, Long categoryId) {
        List<Product> products;
        if (branchId != null && categoryId != null) {
            products = productRepository.findByBranchIdAndCategoryId(branchId, categoryId);
        } else if (branchId != null) {
            products = productRepository.findByBranchId(branchId);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        } else {
            products = productRepository.findAll();
        }
        return products.stream().map(productMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest dto) {
        log.info("Creating product: {}", dto.getName());
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Category not found"));
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Branch not found"));
        
        Product product = productMapper.toEntity(dto, category, branch);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void updateStock(Long productId, Integer quantity) {
        log.info("Updating stock for product {}: {}", productId, quantity);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Product not found"));
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
    }
}
