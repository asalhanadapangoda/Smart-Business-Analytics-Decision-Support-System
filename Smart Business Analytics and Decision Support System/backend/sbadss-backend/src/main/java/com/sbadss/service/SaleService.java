package com.sbadss.service;

import com.sbadss.dto.SaleRequest;
import com.sbadss.entity.Sale;

public interface SaleService {
    Sale createSale(SaleRequest request);
    Sale completeSale(Long saleId);
    void deleteSale(Long saleId);
}
