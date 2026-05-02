package com.sbadss.service;

import com.sbadss.dto.SaleRequest;
import com.sbadss.entity.Sale;

import java.util.List;
import com.sbadss.dto.SaleResponse;

public interface SaleService {
    SaleResponse createSale(SaleRequest request);
    SaleResponse completeSale(Long saleId);
    void deleteSale(Long saleId);
    java.io.ByteArrayInputStream getInvoicePdf(Long saleId);
    List<SaleResponse> getAllSales(Long branchId);
}
