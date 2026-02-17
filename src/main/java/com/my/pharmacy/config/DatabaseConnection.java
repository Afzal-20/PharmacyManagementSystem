package com.my.pharmacy.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    // We keep your V2 filename
    private static final String URL = "jdbc:sqlite:pharmacy_v2.db";

    /**
     * Returns a NEW connection every time.
     * Essential for transactions (Sales) to work without locking the whole app.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Ensure driver is loaded
            Class.forName("org.sqlite.JDBC");

            // Create a fresh connection
            Connection conn = DriverManager.getConnection(URL);

            // Keep your WAL optimization (Great for concurrency)
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA foreign_keys=ON;"); // Enforce relationships
            }

            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found!", e);
        }
    }
}