package com.sbadss.service;

import com.sbadss.dto.SaleRequest;
import com.sbadss.entity.Sale;

import java.util.List;
import com.sbadss.dto.SaleResponse;

public interface SaleService {
    Sale createSale(SaleRequest request);
    Sale completeSale(Long saleId);
    void deleteSale(Long saleId);
    List<SaleResponse> getAllSales(Long branchId);
}
