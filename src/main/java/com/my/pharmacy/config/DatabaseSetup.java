package com.my.pharmacy.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

    // UPDATE: Matched to "pharmacy_v2.db" so it talks to the same file as DatabaseConnection
    private static final String DB_URL = "jdbc:sqlite:pharmacy_v2.db";

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Enable WAL mode here too for consistency
            stmt.execute("PRAGMA journal_mode=WAL;");

            // --- 1. Users (Admins & Salesmen) ---
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "role TEXT NOT NULL CHECK (role IN ('ADMIN', 'SALESMAN')), " +
                    "pin_code TEXT, " +
                    "full_name TEXT" +
                    ");");

            // --- 2. Suppliers (Companies) ---
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "contact_person TEXT, " +
                    "phone TEXT, " +
                    "address TEXT, " +
                    "ledger_balance REAL DEFAULT 0.0" +
                    ");");

            // --- 3. Customers (Retail & Wholesale) ---
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "phone TEXT, " +
                    "address TEXT, " +
                    "type TEXT CHECK (type IN ('RETAIL', 'WHOLESALE')) DEFAULT 'RETAIL', " +
                    "credit_limit REAL DEFAULT 0.0, " +
                    "current_balance REAL DEFAULT 0.0" +
                    ");");

            // --- 4. Products (Base Info) ---
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "generic_name TEXT, " +
                    "manufacturer TEXT, " +
                    "description TEXT, " +
                    "pack_size INTEGER DEFAULT 1, " +
                    "min_stock_level INTEGER DEFAULT 10, " +
                    "shelf_location TEXT" +
                    ");");

            // --- 5. Batches (Pricing Engine) ---
            stmt.execute("CREATE TABLE IF NOT EXISTS batches (" +
                    "batch_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "product_id INTEGER, " +
                    "batch_no TEXT NOT NULL, " +
                    "expiry_date TEXT NOT NULL, " +
                    "qty_on_hand INTEGER DEFAULT 0, " +
                    "cost_price REAL DEFAULT 0.0, " +
                    "trade_price REAL DEFAULT 0.0, " +
                    "retail_price REAL DEFAULT 0.0, " +
                    "tax_percent REAL DEFAULT 0.0, " +
                    "is_active BOOLEAN DEFAULT 1, " +
                    "FOREIGN KEY(product_id) REFERENCES products(id)" +
                    ");");

            // --- 6. Stock Adjustments (Audit Trail) ---
            stmt.execute("CREATE TABLE IF NOT EXISTS stock_adjustments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "batch_id INTEGER, " +
                    "old_qty INTEGER, " +
                    "new_qty INTEGER, " +
                    "reason TEXT, " +
                    "user_id INTEGER, " +
                    "adjustment_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(batch_id) REFERENCES batches(batch_id), " +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");");

            // --- 7. Sales (Transactions) ---
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sale_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "total_amount REAL, " +
                    "payment_mode TEXT, " +
                    "customer_id INTEGER, " +
                    "salesman_id INTEGER, " +
                    "FOREIGN KEY(customer_id) REFERENCES customers(id), " +
                    "FOREIGN KEY(salesman_id) REFERENCES users(id)" +
                    ");");

            // --- 8. Sale Items (Line Items) ---
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sale_id INTEGER, " +
                    "product_id INTEGER, " +
                    "batch_id INTEGER, " +
                    "quantity INTEGER, " +
                    "unit_price REAL, " +
                    "sub_total REAL, " +
                    "bonus_qty INTEGER DEFAULT 0, " +
                    "discount_percent REAL DEFAULT 0.0, " +
                    "FOREIGN KEY(sale_id) REFERENCES sales(id), " +
                    "FOREIGN KEY(product_id) REFERENCES products(id), " +
                    "FOREIGN KEY(batch_id) REFERENCES batches(batch_id)" +
                    ");");

            System.out.println("✅ Hybrid Database Initialized Successfully in pharmacy_v2.db!");

            // --- SEED DATA ---
            if (!stmt.executeQuery("SELECT count(*) FROM users").next()) {
                insertDummyData(stmt);
            }

        } catch (SQLException e) {
            System.err.println("❌ Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void insertDummyData(Statement stmt) throws SQLException {
        stmt.execute("INSERT INTO users (username, password, role, full_name) VALUES ('admin', 'admin123', 'ADMIN', 'System Owner')");
        stmt.execute("INSERT INTO users (username, password, role, full_name) VALUES ('ali', '1234', 'SALESMAN', 'Ali Khan')");
        stmt.execute("INSERT INTO suppliers (name, contact_person) VALUES ('GSK Pakistan', 'Mr. Ahmed')");
        stmt.execute("INSERT INTO products (name, generic_name, manufacturer, pack_size) VALUES ('Panadol Extra', 'Paracetamol', 'GSK', 100)");
        // Cost: 10, Trade: 12, Retail: 15
        stmt.execute("INSERT INTO batches (product_id, batch_no, expiry_date, qty_on_hand, cost_price, trade_price, retail_price) " +
                "VALUES (1, 'BATCH-001', '2026-12-31', 500, 10.0, 12.0, 15.0)");
        System.out.println("✅ Dummy Data Inserted.");
    }
}