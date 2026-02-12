package com.my.pharmacy.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // The single instance of the connection (Singleton Pattern)
    private static Connection instance = null;

    // The name of the database file. SQLite will create this in the project root.
    private static final String DB_URL = "jdbc:sqlite:pharmacy.db";

    // Private constructor prevents other classes from saying "new DatabaseConnection()"
    private DatabaseConnection() {}

    /**
     * Returns the active database connection.
     * If one doesn't exist, it creates it.
     */
    public static Connection getInstance() {
        try {
            if (instance == null || instance.isClosed()) {
                // 1. Load the SQLite Driver (Crucial step!)
                Class.forName("org.sqlite.JDBC");

                // 2. Establish the connection
                instance = DriverManager.getConnection(DB_URL);

                // 3. Performance & Safety Settings
                // Enable Foreign Keys (SQLite disables them by default)
                instance.createStatement().execute("PRAGMA foreign_keys = ON;");
                // Enable Write-Ahead Logging (WAL) for better concurrency
                instance.createStatement().execute("PRAGMA journal_mode = WAL;");

                System.out.println("Database connected successfully.");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("CRITICAL: Database Connection Failed!");
            e.printStackTrace();
            // In a real app, you would show a popup here.
        }
        return instance;
    }

    /**
     * Call this when shutting down the app to close the file safely.
     */
    public static void closeConnection() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}