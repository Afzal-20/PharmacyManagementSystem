package com.my.pharmacy.util;

import com.my.pharmacy.model.Customer;
import com.my.pharmacy.model.Sale;
import com.my.pharmacy.model.SaleItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.print.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.print.attribute.HashPrintRequestAttributeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.print.attribute.PrintRequestAttributeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.print.attribute.standard.JobName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;

/**
 * ThermalPrinter — Sends ESC/POS commands directly to the
 * Black Copper Turbo (BC-85AC-G1), 80mm USB thermal printer.
 *
 * ESC/POS is the native command language of this printer (per its label).
 * We bypass PDF entirely: commands are sent as raw bytes to the Windows
 * print queue, which is the fastest and most reliable method.
 *
 * The printer name in Windows (Devices & Printers) is read from config.properties:
 *   printer.name=YOUR_PRINTER_NAME_HERE
 *
 * If the printer is not found, a clear error is logged and printing is skipped
 * gracefully — the sale is already saved regardless.
 */
public class ThermalPrinter {
    private static final Logger log = LoggerFactory.getLogger(ThermalPrinter.class);

    // ── ESC/POS Command Constants ─────────────────────────────────────────────
    private static final byte ESC  = 0x1B;
    private static final byte GS   = 0x1D;
    private static final byte LF   = 0x0A; // Line feed (newline)

    // Initialization
    private static final byte[] INIT            = {ESC, '@'};

    // Alignment
    private static final byte[] ALIGN_LEFT      = {ESC, 'a', 0};
    private static final byte[] ALIGN_CENTER    = {ESC, 'a', 1};
    private static final byte[] ALIGN_RIGHT     = {ESC, 'a', 2};

    // Text emphasis
    private static final byte[] BOLD_ON         = {ESC, 'E', 1};
    private static final byte[] BOLD_OFF        = {ESC, 'E', 0};

    // Font size — double height + double width for shop name
    private static final byte[] DOUBLE_SIZE     = {GS, '!', 0x11}; // 2x width, 2x height
    private static final byte[] NORMAL_SIZE     = {GS, '!', 0x00};

    // Paper cut (partial cut — safer for the cutter mechanism)
    private static final byte[] PARTIAL_CUT     = {GS, 'V', 66, 5};

    // Paper width: 80mm → printable chars at default font ≈ 48 chars
    private static final int    LINE_WIDTH       = 48;
    private static final String DIVIDER          = "-".repeat(LINE_WIDTH);
    private static final String THICK_DIVIDER    = "=".repeat(LINE_WIDTH);

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Prints a sale invoice. Called from POSController after checkout.
     *
     * @param sale     The completed sale (must have items populated).
     * @param customer The customer (may be walk-in).
     * @param jobLabel Windows print queue label, e.g. "Invoice #42".
     */
    public static void printInvoice(Sale sale, Customer customer, String jobLabel) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            // Shop header
            String shopName    = ConfigUtil.get("pharmacy.name",    "MY PHARMACY");
            String shopAddress = ConfigUtil.get("pharmacy.address",  "Shop Address");
            String shopPhone   = ConfigUtil.get("pharmacy.phone",    "0000-0000000");

            write(buffer, INIT);

            // ── Shop Name (large, centered) ──
            write(buffer, ALIGN_CENTER);
            write(buffer, DOUBLE_SIZE);
            write(buffer, BOLD_ON);
            writeLine(buffer, shopName);
            write(buffer, NORMAL_SIZE);
            write(buffer, BOLD_OFF);

            writeLine(buffer, shopAddress);
            writeLine(buffer, "Ph: " + shopPhone);
            writeLine(buffer, THICK_DIVIDER);

            // ── Invoice metadata ──
            write(buffer, ALIGN_LEFT);
            String dateStr = new SimpleDateFormat("dd-MMM-yyyy  HH:mm").format(sale.getSaleDate());
            writeLine(buffer, "Inv #: " + sale.getId() + "   " + dateStr);

            String custName = (customer != null && customer.getId() != 1)
                    ? customer.getName()
                    : "Counter Sale (Walk-in)";
            writeLine(buffer, "Customer: " + custName);
            writeLine(buffer, DIVIDER);

            // ── Column headers ──
            write(buffer, BOLD_ON);
            writeLine(buffer, formatRow("Item", "Qty", "Rate", "Net"));
            write(buffer, BOLD_OFF);
            writeLine(buffer, DIVIDER);

            // ── Line items ──
            double grandTotal = 0;
            for (SaleItem item : sale.getItems()) {
                String name = truncate(item.getProductName(), 20);
                String qty  = String.valueOf(item.getQuantity());
                String rate = String.format("%.0f", item.getUnitPrice());
                String net  = String.format("%.0f", item.getSubTotal());

                writeLine(buffer, formatRow(name, qty, rate, net));

                // Show bonus and discount on sub-lines if applicable
                if (item.getBonusQty() > 0) {
                    writeLine(buffer, "  + " + item.getBonusQty() + " Bonus");
                }
                if (item.getDiscountPercent() > 0) {
                    writeLine(buffer, "  Disc: " + String.format("%.1f%%", item.getDiscountPercent()));
                }

                grandTotal += item.getSubTotal();
            }

            writeLine(buffer, THICK_DIVIDER);

            // ── Totals ──
            write(buffer, BOLD_ON);
            write(buffer, ALIGN_RIGHT);
            writeLine(buffer, String.format("TOTAL:  Rs. %,.0f", grandTotal));

            double paid = sale.getAmountPaid();
            double balance = sale.getBalanceDue();

            writeLine(buffer, String.format("Paid:   Rs. %,.0f", paid));

            if (balance > 0) {
                writeLine(buffer, String.format("Khata:  Rs. %,.0f", balance));
            } else if (paid > grandTotal) {
                writeLine(buffer, String.format("Change: Rs. %,.0f", paid - grandTotal));
            } else {
                writeLine(buffer, "Fully Paid");
            }

            write(buffer, BOLD_OFF);
            write(buffer, ALIGN_CENTER);

            // ── Footer ──
            writeLine(buffer, DIVIDER);
            writeLine(buffer, "Thank You!");
            writeLine(buffer, "");
            writeLine(buffer, "");
            writeLine(buffer, "");

            // Cut
            write(buffer, PARTIAL_CUT);

            sendToWindowsPrinter(buffer.toByteArray(), jobLabel);

        } catch (IOException e) {
            System.err.println("ThermalPrinter: Failed to build invoice ESC/POS data — " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prints a return/refund slip. Called from SalesHistoryController.
     */
    public static void printReturnReceipt(Sale invoice, SaleItem item,
                                          int returnedQty, double refundAmount,
                                          String refundMethod, String reason) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            String shopName = ConfigUtil.get("pharmacy.name", "MY PHARMACY");
            String shopPhone = ConfigUtil.get("pharmacy.phone", "0000-0000000");

            write(buffer, INIT);
            write(buffer, ALIGN_CENTER);
            write(buffer, BOLD_ON);
            writeLine(buffer, shopName);
            write(buffer, BOLD_OFF);
            writeLine(buffer, shopPhone);
            writeLine(buffer, THICK_DIVIDER);

            write(buffer, ALIGN_LEFT);
            write(buffer, BOLD_ON);
            writeLine(buffer, "   SALES RETURN / REFUND");
            write(buffer, BOLD_OFF);
            writeLine(buffer, DIVIDER);

            String dateStr = new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new java.util.Date());
            writeLine(buffer, "Date:         " + dateStr);
            writeLine(buffer, "Original Inv: #" + invoice.getId());
            writeLine(buffer, DIVIDER);

            writeLine(buffer, "Item:         " + truncate(item.getProductName(), 30));
            writeLine(buffer, "Qty Returned: " + returnedQty);
            writeLine(buffer, String.format("Unit Price:   Rs. %.2f", item.getUnitPrice()));
            writeLine(buffer, DIVIDER);

            write(buffer, BOLD_ON);
            writeLine(buffer, String.format("Refund Total: Rs. %.2f", refundAmount));
            write(buffer, BOLD_OFF);
            writeLine(buffer, "Method:       " + refundMethod);

            if (reason != null && !reason.trim().isEmpty()) {
                writeLine(buffer, "Reason:       " + reason);
            }

            writeLine(buffer, DIVIDER);
            write(buffer, ALIGN_CENTER);

            String footer = "KHATA CREDIT".equals(refundMethod)
                    ? "Khata Credit Applied."
                    : "Cash Refund Issued.";
            writeLine(buffer, footer);
            writeLine(buffer, "Thank You!");
            writeLine(buffer, "");
            writeLine(buffer, "");
            writeLine(buffer, "");

            write(buffer, PARTIAL_CUT);

            sendToWindowsPrinter(buffer.toByteArray(), "Return - Inv #" + invoice.getId());

        } catch (IOException e) {
            System.err.println("ThermalPrinter: Failed to build return receipt data — " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Sends raw ESC/POS bytes to the configured Windows printer.
     * Printer name is read from config.properties → printer.name
     */
    private static void sendToWindowsPrinter(byte[] data, String jobName) {
        String printerName = ConfigUtil.get("printer.name", "").trim();

        if (printerName.isEmpty()) {
            System.err.println("ThermalPrinter: printer.name is not set in config.properties. Skipping print.");
            return;
        }

        // Find the printer by name in the Windows print service list
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);

        PrintService targetService = null;
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                targetService = service;
                break;
            }
        }

        if (targetService == null) {
            System.err.println("ThermalPrinter: Printer '" + printerName + "' not found.");
            System.err.println("Available printers on this system:");
            for (PrintService s : services) {
                System.err.println("  → " + s.getName());
            }
            System.err.println("Update 'printer.name' in config.properties to match one of the above.");
            return;
        }

        try {
            DocPrintJob printJob = targetService.createPrintJob();
            Doc doc = new SimpleDoc(data, flavor, null);

            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
            attrs.add(new JobName(jobName, null));

            printJob.print(doc, attrs);
            System.out.println("✅ ThermalPrinter: Job '" + jobName + "' sent to '" + printerName + "'.");

        } catch (PrintException e) {
            System.err.println("ThermalPrinter: PrintException — " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Formats a 4-column receipt row to fit exactly LINE_WIDTH characters.
     * Columns: Item (left-aligned, wide) | Qty | Rate | Net (right-aligned)
     */
    private static String formatRow(String item, String qty, String rate, String net) {
        // Column widths: item=22, qty=5, rate=9, net=10  → total=46 + 2 spaces = 48
        return String.format("%-22s %4s %8s %10s", truncate(item, 22), qty, rate, net);
    }

    /** Writes bytes to the output stream. */
    private static void write(ByteArrayOutputStream out, byte[] bytes) throws IOException {
        out.write(bytes);
    }

    /** Writes a text line followed by a line feed. */
    private static void writeLine(ByteArrayOutputStream out, String text) throws IOException {
        out.write(text.getBytes("UTF-8"));
        out.write(LF);
    }

    /** Truncates a string to maxLen characters. */
    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }
}