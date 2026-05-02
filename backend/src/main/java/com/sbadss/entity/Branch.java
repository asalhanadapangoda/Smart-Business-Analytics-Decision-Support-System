package com.sbadss.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "branches")
public class Branch extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true)
    private String branchCode;

    @Column(nullable = false)
    private String location;

    private String contactNumber;

    @Column(name = "tax_rate")
    private java.math.BigDecimal taxRate = java.math.BigDecimal.ZERO;

    @Column(name = "is_active")
    private boolean active = true;
}
