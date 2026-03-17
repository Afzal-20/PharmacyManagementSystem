package com.my.pharmacy.util;

import java.util.List;

/**
 * CalculationEngine — Single source of truth for ALL business math in PharmDesk.
 *
 * Every financial calculation in the app must go through this class.
 * No inline arithmetic in controllers or utilities — name the formula here,
 * call it everywhere. This makes logic testable, auditable, and change-proof.
 */
public class CalculationEngine {

    // ── 1. ITEM LEVEL ─────────────────────────────────────────────────────────

    public static double calculateGrossTotal(double rate, int quantity) {
        return rate * quantity;
    }

    public static double calculateDiscountAmount(double grossTotal, double discountPercent) {
        if (discountPercent <= 0) return 0.0;
        return grossTotal * (discountPercent / 100.0);
    }

    public static double calculateNetTotal(double grossTotal, double discountAmount) {
        return grossTotal - discountAmount;
    }

    // ── 2. CART / INVOICE LEVEL ───────────────────────────────────────────────

    public static double calculateGrandTotal(List<Double> netTotals) {
        return netTotals.stream().mapToDouble(Double::doubleValue).sum();
    }

    // ── 3. PAYMENT & KHATA ────────────────────────────────────────────────────

    public static double calculateBalanceDue(double totalBill, double amountPaid) {
        return totalBill - amountPaid;
    }

    public static double calculateChangeDue(double totalBill, double amountPaid) {
        return amountPaid - totalBill;
    }

    // ── 4. PURCHASE / INVENTORY ───────────────────────────────────────────────

    /**
     * Net cost per box after applying company discount and sales tax.
     * Formula: (baseCost - companyDiscount%) + salesTax% on the discounted amount.
     */
    public static double calculateNetPurchaseCost(double baseCost,
                                                  double companyDiscountPercent,
                                                  double salesTaxPercent) {
        double afterDiscount = baseCost - (baseCost * (companyDiscountPercent / 100.0));
        return afterDiscount + (afterDiscount * (salesTaxPercent / 100.0));
    }

    /**
     * Trade price from cost price and margin percentage.
     * Used in Purchase entry and Add Product screens.
     */
    public static double calculateTradePrice(double costPrice, double marginPercent) {
        return costPrice + (costPrice * (marginPercent / 100.0));
    }

    /**
     * Total amount payable to a dealer for a purchase order.
     */
    public static double calculateTotalPayableToDealer(double netBoxCost, int totalBoxes) {
        return netBoxCost * totalBoxes;
    }

    // ── 5. RETURNS ────────────────────────────────────────────────────────────

    /**
     * Full discount-adjusted value of the goods being returned.
     */
    public static double calculateRefundAmount(double rate, int returnedQuantity,
                                               double discountPercent) {
        double grossRefund      = calculateGrossTotal(rate, returnedQuantity);
        double discountRecovery = calculateDiscountAmount(grossRefund, discountPercent);
        return calculateNetTotal(grossRefund, discountRecovery);
    }

    /**
     * Proportional cash to refund for a cash-refund return.
     * Only the cash the pharmacy actually received for this item is returned.
     *
     * @param amountPaid       Total cash paid on the original invoice
     * @param itemSubTotal     This item's subtotal on the invoice
     * @param invoiceTotal     The invoice's grand total
     * @param returnRatio      Fraction of the item being returned (returnQty / originalQty)
     */
    public static double calculateProportionalCashRefund(double amountPaid,
                                                         double itemSubTotal,
                                                         double invoiceTotal,
                                                         double returnRatio) {
        if (invoiceTotal == 0) return 0.0;
        return amountPaid * (itemSubTotal / invoiceTotal) * returnRatio;
    }

    /**
     * Proportional khata (credit balance) to cancel for a return.
     * Represents the outstanding debt portion attributable to the returned item.
     *
     * @param balanceDue       Outstanding khata balance on the original invoice
     * @param itemSubTotal     This item's subtotal on the invoice
     * @param invoiceTotal     The invoice's grand total
     * @param returnRatio      Fraction of the item being returned (returnQty / originalQty)
     */
    public static double calculateProportionalKhataReduction(double balanceDue,
                                                             double itemSubTotal,
                                                             double invoiceTotal,
                                                             double returnRatio) {
        if (invoiceTotal == 0) return 0.0;
        return balanceDue * (itemSubTotal / invoiceTotal) * returnRatio;
    }

    // ── 6. DASHBOARD / CLOSING ────────────────────────────────────────────────

    /**
     * Net cash in drawer at end of day.
     * = cash collected at POS today + old khata recovered today - refunds paid out
     */
    public static double calculateNetCashInDrawer(double cashFromSales,
                                                  double cashRecovered,
                                                  double cashRefunds) {
        return cashFromSales + cashRecovered - cashRefunds;
    }

    // ── 7. INVOICE PDF HELPERS ────────────────────────────────────────────────

    /**
     * Absolute discount amount for a line item on the invoice PDF summary row.
     * = (unit price × qty) - subtotal
     */
    public static double calculateAbsoluteDiscount(double unitPrice, int quantity,
                                                   double subTotal) {
        return calculateGrossTotal(unitPrice, quantity) - subTotal;
    }

    /**
     * Grand total by accumulating subtotals from a list of items.
     * Used by ThermalPrinter to sum line items without a CartService reference.
     */
    public static double calculateGrandTotalFromItems(List<com.my.pharmacy.model.SaleItem> items) {
        return items.stream().mapToDouble(com.my.pharmacy.model.SaleItem::getSubTotal).sum();
    }

    // ── 8. RETURN RATIO ───────────────────────────────────────────────────────

    /**
     * Fraction of an item being returned.
     * e.g. returning 3 of 10 boxes = 0.3
     *
     * @param returnQty    Number of units being returned
     * @param originalQty  Total units on the original sale line
     */
    public static double calculateReturnRatio(int returnQty, int originalQty) {
        if (originalQty == 0) return 0.0;
        return (double) returnQty / originalQty;
    }
}