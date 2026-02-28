package com.my.pharmacy.util;

import java.util.List;

public class CalculationEngine {

    // --- 1. ITEM LEVEL MATH ---
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

    // --- 2. CART / INVOICE LEVEL MATH ---
    public static double calculateGrandTotal(List<Double> netTotals) {
        return netTotals.stream().mapToDouble(Double::doubleValue).sum();
    }

    // --- 3. PAYMENT & KHATA MATH ---
    public static double calculateBalanceDue(double totalBill, double amountPaid) {
        return totalBill - amountPaid;
    }

    public static double calculateChangeDue(double totalBill, double amountPaid) {
        return amountPaid - totalBill;
    }

    // --- 4. PURCHASE / INVENTORY MATH ---
    public static double calculateNetPurchaseCost(double baseCost, double companyDiscountPercent, double salesTaxPercent) {
        double afterDiscount = baseCost - (baseCost * (companyDiscountPercent / 100.0));
        return afterDiscount + (afterDiscount * (salesTaxPercent / 100.0));
    }

    // --- 5. FUTURE: RETURNS MATH ---
    public static double calculateRefundAmount(double rate, int returnedQuantity, double discountPercent) {
        double grossRefund = calculateGrossTotal(rate, returnedQuantity);
        double discountRecovery = calculateDiscountAmount(grossRefund, discountPercent);
        return calculateNetTotal(grossRefund, discountRecovery);
    }
}