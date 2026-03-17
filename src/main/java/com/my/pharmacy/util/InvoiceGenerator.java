package com.my.pharmacy.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.my.pharmacy.model.Customer;
import com.my.pharmacy.model.Sale;
import com.my.pharmacy.model.SaleItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileOutputStream;

public class InvoiceGenerator {
    private static final Logger log = LoggerFactory.getLogger(InvoiceGenerator.class);

    // 80mm thermal paper width is ~226 points.
    private static final Rectangle THERMAL_ROLL   = new Rectangle(226, 1000);
    private static final Font BOLD_FONT           = new Font(Font.FontFamily.HELVETICA, 8,  Font.BOLD);
    private static final Font NORMAL_FONT         = new Font(Font.FontFamily.HELVETICA, 8,  Font.NORMAL);
    private static final Font TITLE_FONT          = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
    private static final Font GRAND_TOTAL_FONT    = new Font(Font.FontFamily.HELVETICA, 9,  Font.BOLD);

    public static void generateThermalReceipt(Sale sale, Customer customer, String destFilePath) {
        try {
            new java.io.File(AppPaths.INVOICES_DIR).mkdirs();

            Document document = new Document(THERMAL_ROLL, 8, 8, 10, 10);
            PdfWriter.getInstance(document, new FileOutputStream(destFilePath));
            document.open();

            // --- 1. HEADER ---
            String shopName    = ConfigUtil.get("pharmacy.name",    "MY PHARMACY");
            String shopAddress = ConfigUtil.get("pharmacy.address", "Shop Address Here");
            String shopPhone   = ConfigUtil.get("pharmacy.phone",   "0000-0000000");

            Paragraph header = new Paragraph();
            header.setAlignment(Element.ALIGN_CENTER);
            header.add(new Chunk(shopName    + "\n", TITLE_FONT));
            header.add(new Chunk(shopAddress + "\n", NORMAL_FONT));
            header.add(new Chunk(shopPhone   + "\n", NORMAL_FONT));
            document.add(header);
            document.add(new Paragraph("\n"));

            // --- 2. INVOICE BOX ---
            PdfPTable invoiceBox = new PdfPTable(1);
            invoiceBox.setWidthPercentage(80);
            PdfPCell boxCell = new PdfPCell(new Phrase("I N V O I C E", BOLD_FONT));
            boxCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            boxCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            boxCell.setPaddingTop(3);
            boxCell.setPaddingBottom(5);
            boxCell.setBorderWidth(0.5f);
            invoiceBox.addCell(boxCell);
            document.add(invoiceBox);
            document.add(new Paragraph("\n"));

            // --- 3. META INFO ---
            String custName = (customer != null && customer.getId() != 1)
                    ? customer.getId() + " / " + customer.getName()
                    : "1 / COUNTER SALE";
            String custAdd = (customer != null && customer.getAddress() != null
                    && !customer.getAddress().isEmpty()) ? customer.getAddress() : "";

            // FIX: use TimeUtil so invoice shows local date, not raw UTC
            String dateStr = TimeUtil.format(sale.getSaleDate(), TimeUtil.PATTERN_DATE);

            document.add(new Paragraph("Customer: " + custName, NORMAL_FONT));
            if (!custAdd.isEmpty()) {
                document.add(new Paragraph("Add: " + custAdd, NORMAL_FONT));
            }
            if (customer != null && customer.getCnic() != null && !customer.getCnic().isEmpty()) {
                document.add(new Paragraph("CNIC: " + customer.getCnic(), NORMAL_FONT));
            }

            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingBefore(5);
            metaTable.setSpacingAfter(5);

            PdfPCell invCell = new PdfPCell(new Phrase("Inv No:   " + sale.getId(), NORMAL_FONT));
            invCell.setBorder(Rectangle.NO_BORDER);
            invCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            metaTable.addCell(invCell);

            PdfPCell dateCell = new PdfPCell(new Phrase("Date: " + dateStr, NORMAL_FONT));
            dateCell.setBorder(Rectangle.NO_BORDER);
            dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            metaTable.addCell(dateCell);

            document.add(metaTable);

            // --- 4. ITEMS TABLE ---
            float[] columnWidths = {4.3f, 1.7f, 1.0f, 1.3f, 1.7f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(98);

            addHeaderCell(table, "Name", Element.ALIGN_LEFT);
            addHeaderCell(table, "STP",  Element.ALIGN_RIGHT);
            addHeaderCell(table, "Qty",  Element.ALIGN_CENTER);
            addHeaderCell(table, "Disc", Element.ALIGN_CENTER);
            addHeaderCell(table, "Net",  Element.ALIGN_RIGHT);

            double sumStp  = 0.0;
            int    sumQty  = 0;
            double sumDisc = 0.0;

            for (SaleItem item : sale.getItems()) {
                double rawTotal         = CalculationEngine.calculateGrossTotal(item.getUnitPrice(), item.getQuantity());
                double absoluteDiscount = CalculationEngine.calculateAbsoluteDiscount(item.getUnitPrice(), item.getQuantity(), item.getSubTotal());

                addItemCell(table, item.getProductName(),                         Element.ALIGN_LEFT);
                addItemCell(table, String.format("%.2f",  item.getUnitPrice()),   Element.ALIGN_RIGHT);
                addItemCell(table, String.valueOf(item.getQuantity()),             Element.ALIGN_CENTER);
                addItemCell(table, String.format("%.2f",  absoluteDiscount),      Element.ALIGN_CENTER);
                addItemCell(table, String.format("%,.2f", item.getSubTotal()),     Element.ALIGN_RIGHT);

                sumStp  += rawTotal;
                sumQty  += item.getQuantity();
                sumDisc += absoluteDiscount;
            }

            // Footer Summary Row
            addFooterCell(table, "TOTAL: Items: " + sale.getItems().size(), Element.ALIGN_LEFT);
            addFooterCell(table, String.format("%.2f",  sumStp),               Element.ALIGN_RIGHT);
            addFooterCell(table, String.valueOf(sumQty),                        Element.ALIGN_CENTER);
            addFooterCell(table, String.format("%.2f",  sumDisc),              Element.ALIGN_CENTER);
            addFooterCell(table, String.format("%,.2f", sale.getTotalAmount()), Element.ALIGN_RIGHT);

            document.add(table);
            document.add(new Paragraph("\n"));

            // --- 5. GRAND TOTAL ---
            Paragraph grandTotal = new Paragraph(
                    String.format("%,.2f", sale.getTotalAmount()), GRAND_TOTAL_FONT);
            grandTotal.setAlignment(Element.ALIGN_RIGHT);
            document.add(grandTotal);

            document.close();
            log.info("Invoice PDF saved: {}", destFilePath);

        } catch (Exception e) {
            log.error("Failed to generate invoice PDF: {}", e.getMessage(), e);
        }
    }

    // ── Table cell helpers ────────────────────────────────────────────────────

    private static void addHeaderCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BOLD_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.5f);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private static void addItemCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
        cell.setBorderWidthLeft(0.5f);
        cell.setBorderWidthRight(0.5f);
        cell.setPaddingTop(3);
        cell.setPaddingBottom(3);
        cell.setPaddingLeft(3);
        cell.setPaddingRight(3);
        table.addCell(cell);
    }

    private static void addFooterCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BOLD_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.5f);
        cell.setPadding(4);
        table.addCell(cell);
    }

    // ── Return Receipt ────────────────────────────────────────────────────────

    public static void generateReturnReceipt(Sale invoice, SaleItem item, int returnedQty,
                                             double refundAmount, String refundMethod, String reason) {
        String fileName = AppPaths.returnReceiptPath(invoice.getId());
        new java.io.File(AppPaths.RETURNS_DIR).mkdirs();

        Document document = new Document(new Rectangle(226, 400), 10, 10, 15, 15);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            Font headerFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font standardFont = FontFactory.getFont(FontFactory.HELVETICA,      9);
            Font boldFont     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

            String shopName    = ConfigUtil.get("pharmacy.name",    "MY PHARMACY");
            String shopAddress = ConfigUtil.get("pharmacy.address", "Shop Address");
            String shopPhone   = ConfigUtil.get("pharmacy.phone",   "0000-0000000");

            Paragraph header = new Paragraph(
                    shopName + "\n" + shopAddress + "\nPh: " + shopPhone + "\n\n", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            document.add(new Paragraph("SALES RETURN / REFUND", boldFont));
            // FIX: use TimeUtil for consistent local time — replaces LocalDateTime.now() with wrong pattern
            document.add(new Paragraph(
                    "Date: " + TimeUtil.format(TimeUtil.nowLocal(), TimeUtil.PATTERN_FULL), standardFont));
            document.add(new Paragraph("Original Inv #: " + invoice.getId(), standardFont));

            document.add(new Chunk(new LineSeparator()));

            document.add(new Paragraph("Item: "           + item.getProductName(), standardFont));
            document.add(new Paragraph("Qty Returned: "   + returnedQty,           standardFont));
            document.add(new Paragraph("Unit Price: Rs. " + item.getUnitPrice(),    standardFont));

            document.add(new Chunk(new LineSeparator()));

            document.add(new Paragraph("Total Refund: Rs. " + refundAmount, boldFont));
            document.add(new Paragraph("Method: "           + refundMethod, standardFont));
            if (reason != null && !reason.trim().isEmpty()) {
                document.add(new Paragraph("Reason: " + reason, standardFont));
            }

            document.add(new Chunk(new LineSeparator()));

            String disclaimer = "KHATA CREDIT".equals(refundMethod)
                    ? "Khata Credit Applied." : "Cash Refund Issued.";
            Paragraph footer = new Paragraph(
                    "Refund Processed Successfully.\n" + disclaimer + "\nThank you!", standardFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            log.info("Return receipt PDF saved: {}", fileName);

        } catch (Exception e) {
            log.error("Failed to generate return receipt PDF: {}", e.getMessage(), e);
        }
    }
}