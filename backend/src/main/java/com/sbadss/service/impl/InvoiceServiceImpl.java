package com.sbadss.service.impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.sbadss.entity.Sale;
import com.sbadss.entity.SaleItem;
import com.sbadss.service.InvoiceService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Override
    public ByteArrayInputStream generateInvoicePdf(Sale sale) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("SMART BUSINESS ANALYTICS & DSS").setBold().setFontSize(20));
            document.add(new Paragraph("Invoice: " + sale.getInvoiceNumber()));
            document.add(new Paragraph("Branch: " + sale.getBranch().getName()));
            document.add(new Paragraph("Date: " + sale.getCreatedAt()));
            document.add(new Paragraph("Cashier: " + sale.getCashier().getFullName()));
            if (sale.getCustomer() != null) {
                document.add(new Paragraph("Customer: " + sale.getCustomer().getName()));
            }
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPointArray(new float[]{3, 1, 1, 1}));
            table.setWidth(UnitValue.createPercentValue(100));
            table.addHeaderCell("Item");
            table.addHeaderCell("Qty");
            table.addHeaderCell("Price");
            table.addHeaderCell("Total");

            for (SaleItem item : sale.getItems()) {
                table.addCell(item.getProduct().getName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(fmt.format(item.getUnitPrice()));
                table.addCell(fmt.format(item.getTotalPrice()));
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Total Amount: " + fmt.format(sale.getTotalAmount())).setBold().setFontSize(14));
            document.add(new Paragraph("Payment Method: " + sale.getPaymentMethod()));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
