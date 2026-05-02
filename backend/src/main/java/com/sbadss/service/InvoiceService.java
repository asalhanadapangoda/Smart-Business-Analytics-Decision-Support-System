package com.sbadss.service;

import com.sbadss.entity.Sale;
import java.io.ByteArrayInputStream;

public interface InvoiceService {
    ByteArrayInputStream generateInvoicePdf(Sale sale);
}
