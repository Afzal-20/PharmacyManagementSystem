package com.my.pharmacy.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // CHANGED: New filename to bypass the lock on the old file
    private static final String URL = "jdbc:sqlite:pharmacy_v2.db";
    private static Connection connection = null;

    public static Connection getInstance() {
        try {
            // Check if connection is closed or null, then create a new one
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
                // Optimization: Enable WAL mode for better concurrency (prevents locks)
                try (java.sql.Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA journal_mode=WAL;");
                }
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Explicitly close the connection when the app shuts down
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}