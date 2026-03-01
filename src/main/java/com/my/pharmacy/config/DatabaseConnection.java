package com.my.pharmacy.config;

import com.my.pharmacy.util.ConfigUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    public static String getDatabaseUrl() {
        // Enforces wholesale database by default
        String dbName = ConfigUtil.get("db.name", "wholesale_pharmacy.db");
        return "jdbc:sqlite:" + dbName;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(getDatabaseUrl());
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA foreign_keys=ON;");
            }
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found!", e);
        }
    }
}