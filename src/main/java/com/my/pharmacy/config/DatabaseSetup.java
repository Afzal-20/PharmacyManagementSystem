package com.my.pharmacy.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseSetup {

    public static void initialize() {
        // Use the dynamic URL created in DatabaseConnection
        String url = DatabaseConnection.getDatabaseUrl();

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            // 1. Products Table (Preserved existing structure)
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "generic_name TEXT, " +
                    "manufacturer TEXT, " +
                    "description TEXT, " +
                    "pack_size INTEGER DEFAULT 1, " +
                    "min_stock_level INTEGER DEFAULT 10, " +
                    "shelf_location TEXT)");

            // 2. Updated Batches Table (Added Tax and Company Discount for professional costing)
            stmt.execute("CREATE TABLE IF NOT EXISTS batches (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "product_id INTEGER, " +
                    "batch_no TEXT, " +
                    "expiry_date TEXT, " +
                    "qty_on_hand INTEGER, " +
                    "cost_price REAL, " +
                    "trade_price REAL, " +
                    "retail_price REAL, " +
                    "company_discount REAL DEFAULT 0.0, " + // Added for Net Cost tracking
                    "sales_tax REAL DEFAULT 0.0, " +        // Added for tax reporting
                    "discount_percent REAL, " +
                    "tax_percent REAL DEFAULT 0.0, " +
                    "is_active INTEGER DEFAULT 1, " +
                    "FOREIGN KEY(product_id) REFERENCES products(id))");

            // 3. Updated Dealers Table (Added Khata Balance tracking)
            stmt.execute("CREATE TABLE IF NOT EXISTS dealers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "company_name TEXT, " +
                    "phone TEXT, " +
                    "address TEXT, " +
                    "license_no TEXT, " +
                    "current_balance REAL DEFAULT 0.0)"); // Ledger balance

            // 4. Updated Customers Table (Added Area tracking and Khata Balance)
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "phone TEXT, " +
                    "address TEXT, " +
                    "type TEXT DEFAULT 'RETAIL', " +
                    "current_balance REAL DEFAULT 0.0, " + // Ledger balance
                    "area_code TEXT, " +                  // For delivery/wholesale routes
                    "area_name TEXT)");

            // 5. Updated Sales Table (Added Payment and Salesman tracking)
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "total_amount REAL, " +
                    "amount_paid REAL DEFAULT 0.0, " + // To determine Khata impact
                    "balance_due REAL DEFAULT 0.0, " + // Outstanding debt for the sale
                    "payment_mode TEXT, " +
                    "customer_id INTEGER, " +
                    "salesman_id INTEGER, " +          // Tracked for commissions
                    "FOREIGN KEY(customer_id) REFERENCES customers(id))");

            // 6. Sale Items Table
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

            // --- MIGRATION LOGIC ---
            // Ensures existing .db files are updated with new columns without data loss
            addColumnIfMissing(stmt, "customers", "current_balance", "REAL DEFAULT 0.0");
            addColumnIfMissing(stmt, "customers", "area_code", "TEXT");
            addColumnIfMissing(stmt, "customers", "area_name", "TEXT");
            addColumnIfMissing(stmt, "dealers", "current_balance", "REAL DEFAULT 0.0");
            addColumnIfMissing(stmt, "batches", "company_discount", "REAL DEFAULT 0.0");
            addColumnIfMissing(stmt, "batches", "sales_tax", "REAL DEFAULT 0.0");
            addColumnIfMissing(stmt, "sales", "amount_paid", "REAL DEFAULT 0.0");
            addColumnIfMissing(stmt, "sales", "balance_due", "REAL DEFAULT 0.0");

            System.out.println("✅ Database structure optimized for Khata and professional accounting.");

        } catch (Exception e) {
            System.err.println("❌ Database Initialization Error:");
            e.printStackTrace();
        }
    }

    /**
     * Helper method to add columns to existing tables if they do not already exist.
     */
    private static void addColumnIfMissing(Statement stmt, String table, String column, String definition) {
        try {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        } catch (SQLException e) {
            // Expected error if the column already exists (Error code for duplicate column)
        }
    }
}