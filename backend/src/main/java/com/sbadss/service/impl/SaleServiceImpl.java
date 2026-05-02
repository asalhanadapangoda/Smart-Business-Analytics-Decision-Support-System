package com.sbadss.service.impl;

import com.sbadss.dto.SaleItemRequest;
import com.sbadss.dto.SaleRequest;
import com.sbadss.entity.*;
import com.sbadss.repository.*;
import com.sbadss.service.SaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.sbadss.dto.SaleResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final com.sbadss.service.NotificationService notificationService;
    private final com.sbadss.service.InvoiceService invoiceService;

    @Override
    @Transactional(readOnly = true)
    public java.io.ByteArrayInputStream getInvoicePdf(Long saleId) {
        log.info("Generating PDF invoice for sale: {}", saleId);
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Sale not found"));
        return invoiceService.generateInvoicePdf(sale);
    }

    @Override
    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        log.info("Creating sale for branch: {}", request.getBranchId());
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Current user for sale: {}", username);

        User cashier = userRepository.findByUsername(username)
                .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("User not found: " + username));
        log.debug("Cashier found: {}", cashier.getId());

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Branch not found: " + request.getBranchId()));
        log.debug("Branch found: {}", branch.getId());

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Customer not found: " + request.getCustomerId()));
            log.debug("Customer found: {}", customer.getId());
        }

        Sale sale = new Sale();
        sale.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        sale.setCashier(cashier);
        sale.setBranch(branch);
        sale.setCustomer(customer);
        sale.setStatus(SaleStatus.DRAFT);
        sale.setPaymentMethod(request.getPaymentMethod() != null ? 
            Sale.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()) : Sale.PaymentMethod.CASH);
        sale.setTotalAmount(BigDecimal.ZERO);
        log.debug("Sale entity initialized with invoice: {} and payment: {}", sale.getInvoiceNumber(), sale.getPaymentMethod());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleItemRequest itemReq : request.getItems()) {
            log.debug("Processing item: ProductID={}, Qty={}", itemReq.getProductId(), itemReq.getQuantity());
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Product not found: " + itemReq.getProductId()));

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                log.warn("Stock insufficient for product {}: need {}, have {}", product.getName(), itemReq.getQuantity(), product.getStockQuantity());
                throw new com.sbadss.exception.BusinessException("Insufficient stock for product: " + product.getName());
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);
            log.debug("Stock deducted for product: {}", product.getName());

            // Check Low Stock Threshold
            if (product.getStockQuantity() <= product.getMinThreshold()) {
                log.info("Low stock alert for product: {} (Current: {}, Threshold: {})", 
                    product.getName(), product.getStockQuantity(), product.getMinThreshold());
                
                String alertTitle = "Low Stock Alert: " + product.getName();
                String alertMsg = String.format("Product '%s' (SKU: %s) is low on stock. Current quantity: %d.", 
                    product.getName(), product.getSku(), product.getStockQuantity());
                
                // Notify current cashier and branch managers
                notificationService.createNotification(alertTitle, alertMsg, Notification.NotificationType.WARNING, cashier);
                
                // Find and notify branch managers/admins
                userRepository.findAll().stream()
                    .filter(u -> u.getRole().getName().equals("ADMIN") || 
                                (u.getRole().getName().equals("MANAGER") && u.getBranch() != null && u.getBranch().getId().equals(branch.getId())))
                    .forEach(u -> {
                        if (!u.getId().equals(cashier.getId())) {
                            notificationService.createNotification(alertTitle, alertMsg, Notification.NotificationType.WARNING, u);
                        }
                    });
            }

            SaleItem item = new SaleItem();
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            
            sale.addItem(item);
            totalAmount = totalAmount.add(item.getTotalPrice());
            log.debug("Item added to sale. Item total: {}", item.getTotalPrice());
        }

        sale.setTotalAmount(totalAmount);
        log.debug("Final sale total: {}", totalAmount);

        // Loyalty Points Logic
        if (customer != null) {
            if (sale.getPaymentMethod() == Sale.PaymentMethod.LOYALTY_POINTS) {
                int pointsNeeded = totalAmount.intValue();
                if (customer.getLoyaltyPoints() < pointsNeeded) {
                    throw new com.sbadss.exception.BusinessException("Insufficient loyalty points. Have: " + customer.getLoyaltyPoints() + ", Need: " + pointsNeeded);
                }
                customer.setLoyaltyPoints(customer.getLoyaltyPoints() - pointsNeeded);
            } else {
                // Earn 1 point per $10
                int pointsEarned = totalAmount.divide(BigDecimal.valueOf(10), 0, java.math.RoundingMode.FLOOR).intValue();
                customer.setLoyaltyPoints(customer.getLoyaltyPoints() + pointsEarned);
                log.info("Customer {} earned {} loyalty points", customer.getName(), pointsEarned);
            }
            customerRepository.save(customer);
        }
        
        try {
            Sale savedSale = saleRepository.save(sale);
            log.info("Sale saved successfully with ID: {}", savedSale.getId());
            return mapToResponse(savedSale);
        } catch (Exception e) {
            log.error("Failed to save sale entity: ", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public SaleResponse completeSale(Long saleId) {
        log.info("Completing sale: {}", saleId);
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Sale not found"));
        sale.setStatus(SaleStatus.COMPLETED);
        return mapToResponse(saleRepository.save(sale));
    }

    private SaleResponse mapToResponse(Sale sale) {
        return SaleResponse.builder()
                .id(sale.getId())
                .invoiceNumber(sale.getInvoiceNumber())
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getName() : null)
                .cashierName(sale.getCashier().getFullName())
                .totalAmount(sale.getTotalAmount())
                .status(sale.getStatus())
                .createdAt(sale.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteSale(Long saleId) {
        log.info("Attempting to delete sale: {}", saleId);
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();

        // Business Rule: Cashiers can only delete if not completed
        if (currentUser.getRole().getName().equals("CASHIER") && sale.getStatus() == SaleStatus.COMPLETED) {
            log.warn("Unauthorized attempt to delete completed sale by cashier: {}", username);
            throw new com.sbadss.exception.BusinessException("Cashiers cannot delete a completed sale.");
        }

        // Restore stock if deleting
        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        saleRepository.delete(sale);
    }

    @Override
    public List<SaleResponse> getAllSales(Long branchId) {
        log.info("Fetching all sales for branch: {}", branchId);
        List<Sale> sales;
        if (branchId != null) {
            sales = saleRepository.findByBranchId(branchId);
        } else {
            sales = saleRepository.findAll();
        }

        return sales.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
}
