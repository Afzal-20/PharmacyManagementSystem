package com.my.pharmacy.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseSetup {

    public static void initialize() {
        // Use the dynamic URL we created earlier
        String url = DatabaseConnection.getDatabaseUrl();

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            // 1. Products Table
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "generic_name TEXT, " +
                    "manufacturer TEXT, " +
                    "description TEXT, " +
                    "pack_size INTEGER DEFAULT 1, " +
                    "min_stock_level INTEGER DEFAULT 10, " +
                    "shelf_location TEXT)");

            // 2. Batches Table (Crucial for "Every Detail" Inventory)
            stmt.execute("CREATE TABLE IF NOT EXISTS batches (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "product_id INTEGER, " +
                    "batch_no TEXT, " +
                    "expiry_date TEXT, " +
                    "qty_on_hand INTEGER, " +
                    "cost_price REAL, " +
                    "trade_price REAL, " +
                    "retail_price REAL, " +
                    "discount_percent REAL, " +
                    "tax_percent REAL DEFAULT 0.0, " + // Added to fix error
                    "is_active INTEGER DEFAULT 1, " +   // Added to fix error
                    "FOREIGN KEY(product_id) REFERENCES products(id))");

            // 3. SEPARATE: Dealers Table (Suppliers)
            stmt.execute("CREATE TABLE IF NOT EXISTS dealers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "company_name TEXT, " +
                    "phone TEXT, " +
                    "address TEXT, " +
                    "license_no TEXT)");

            // 4. SEPARATE: Customers Table (Buyers)
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "phone TEXT, " +
                    "address TEXT, " +
                    "type TEXT DEFAULT 'RETAIL')");

            // 5. Sales & Sale Items (No changes needed here)
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "total_amount REAL, " +
                    "payment_mode TEXT, " +
                    "customer_id INTEGER, " +
                    "salesman_id INTEGER)");

            stmt.execute("CREATE TABLE IF NOT EXISTS sale_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sale_id INTEGER, " +
                    "product_id INTEGER, " +
                    "batch_id INTEGER, " +
                    "quantity INTEGER, " +
                    "unit_price REAL, " +
                    "sub_total REAL, " +
                    "bonus_qty INTEGER DEFAULT 0, " +
                    "discount_percent REAL DEFAULT 0.0)");

            System.out.println("✅ Database structure (Wholesale Optimized) initialized at: " + url);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}