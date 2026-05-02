package com.sbadss.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String sku;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "purchase_price")
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(name = "min_threshold", nullable = false)
    private Integer minThreshold = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "is_active")
    private boolean active = true;
}
