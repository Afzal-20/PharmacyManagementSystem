package com.my.pharmacy.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseSetup {

    public static void initialize() {
        String url = DatabaseConnection.getDatabaseUrl();

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "generic_name TEXT, " +
                    "manufacturer TEXT, " +
                    "description TEXT, " +
                    "pack_size INTEGER DEFAULT 1, " +
                    "min_stock_level INTEGER DEFAULT 10, " +
                    "shelf_location TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS batches (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "product_id INTEGER, " +
                    "batch_no TEXT, " +
                    "expiry_date TEXT, " +
                    "qty_on_hand INTEGER, " +
                    "cost_price REAL, " +
                    "trade_price REAL, " +
                    "retail_price REAL, " +
                    "company_discount REAL DEFAULT 0.0, " +
                    "sales_tax REAL DEFAULT 0.0, " +
                    "discount_percent REAL, " +
                    "tax_percent REAL DEFAULT 0.0, " +
                    "is_active INTEGER DEFAULT 1, " +
                    "FOREIGN KEY(product_id) REFERENCES products(id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS dealers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "company_name TEXT, " +
                    "phone TEXT, " +
                    "address TEXT, " +
                    "license_no TEXT, " +
                    "current_balance REAL DEFAULT 0.0)");

            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "phone TEXT, " +
                    "address TEXT, " +
                    "type TEXT DEFAULT 'RETAIL', " +
                    "current_balance REAL DEFAULT 0.0, " +
                    "area_code TEXT, " +
                    "area_name TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "total_amount REAL, " +
                    "amount_paid REAL DEFAULT 0.0, " +
                    "balance_due REAL DEFAULT 0.0, " +
                    "payment_mode TEXT, " +
                    "customer_id INTEGER, " +
                    "salesman_id INTEGER, " +
                    "FOREIGN KEY(customer_id) REFERENCES customers(id))");

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

            // --- NEW: Payments Table for Khata Management ---
            stmt.execute("CREATE TABLE IF NOT EXISTS payments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "entity_id INTEGER, " +
                    "entity_type TEXT, " + // 'CUSTOMER' or 'DEALER'
                    "payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "amount REAL, " +
                    "payment_mode TEXT, " +
                    "description TEXT)");

            // --- NEW: Audit Table for Stock Adjustments ---
            stmt.execute("CREATE TABLE IF NOT EXISTS stock_adjustments_audit (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "batch_id INTEGER, " +
                    "old_qty INTEGER, " +
                    "new_qty INTEGER, " +
                    "reason TEXT, " +
                    "adjustment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "user_id INTEGER DEFAULT 1, " +
                    "FOREIGN KEY(batch_id) REFERENCES batches(id))");

            // Migration logic
            addColumnIfMissing(stmt, "customers", "cnic", "TEXT");
            addColumnIfMissing(stmt, "customers", "current_balance", "REAL DEFAULT 0.0");
            addColumnIfMissing(stmt, "customers", "area_code", "TEXT");
            addColumnIfMissing(stmt, "customers", "area_name", "TEXT");
            addColumnIfMissing(stmt, "dealers", "current_balance", "REAL DEFAULT 0.0");
            addColumnIfMissing(stmt, "batches", "company_discount", "REAL DEFAULT 0.0");
            addColumnIfMissing(stmt, "batches", "sales_tax", "REAL DEFAULT 0.0");
            addColumnIfMissing(stmt, "sales", "amount_paid", "REAL DEFAULT 0.0");
            addColumnIfMissing(stmt, "sales", "balance_due", "REAL DEFAULT 0.0");

            System.out.println("✅ Database structure optimized for Khata and audit logging.");

        } catch (Exception e) {
            System.err.println("❌ Database Initialization Error:");
            e.printStackTrace();
        }
    }

    private static void addColumnIfMissing(Statement stmt, String table, String column, String definition) {
        try {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        } catch (SQLException e) { }
    }
}