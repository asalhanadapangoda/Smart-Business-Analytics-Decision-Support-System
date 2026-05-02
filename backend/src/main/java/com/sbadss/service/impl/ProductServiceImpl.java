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
        
        // Auto-generate SKU if not provided
        if (product.getSku() == null || product.getSku().isBlank()) {
            String generatedSku = "PRD-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            product.setSku(generatedSku);
        }
        
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

    @Override
    @Transactional
    public void importProducts(org.springframework.web.multipart.MultipartFile file, Long branchId) {
        log.info("Importing products from CSV for branch: {}", branchId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Branch not found"));

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // Skip header
                String[] data = line.split(",");
                if (data.length < 5) continue;

                Product p = new Product();
                p.setName(data[0].trim());
                p.setSku(data[1].trim().isEmpty() ? "PRD-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase() : data[1].trim());
                p.setDescription(data.length > 2 ? data[2].trim() : "");
                p.setPrice(new java.math.BigDecimal(data[3].trim()));
                p.setStockQuantity(Integer.parseInt(data[4].trim()));
                p.setMinThreshold(data.length > 6 ? Integer.parseInt(data[6].trim()) : 5);
                
                if (data.length > 5 && !data[5].trim().isEmpty()) {
                    Category cat = categoryRepository.findById(Long.parseLong(data[5].trim())).orElse(null);
                    p.setCategory(cat);
                }
                
                p.setBranch(branch);
                p.setActive(true);
                productRepository.save(p);
            }
        } catch (Exception e) {
            log.error("Failed to import products", e);
            throw new RuntimeException("CSV Import failed: " + e.getMessage());
        }
    }
}
