package com.my.pharmacy.util;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatterUtil {

    // Create a formatter specifically for Pakistan Rupees
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PK"));

    /**
     * Formats a double value into a Price String.
     * Example: 1500.5 -> "Rs. 1,500.50"
     */
    public static String formatPrice(double price) {
        // The default PK locale might give "PKR 1,500.00", we want "Rs."
        return "Rs. " + String.format("%,.2f", price);
    }

    /**
     * Formats a date string (Optional helper for later)
     */
    public static String formatDate(java.sql.Date date) {
        if (date == null) return "";
        return new java.text.SimpleDateFormat("dd-MMM-yyyy").format(date);
    }
}