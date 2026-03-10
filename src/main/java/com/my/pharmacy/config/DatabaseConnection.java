package com.my.pharmacy.config;

import com.my.pharmacy.util.ConfigUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    public static String getDatabaseUrl() {
        // Always use the AppPaths location (C:\ProgramData\PharmDesk\)
        // so the database is never blocked by Windows UAC.
        // The db.name config entry is kept for the filename only.
        String dbName = ConfigUtil.get("db.name", "wholesale_pharmacy.db");

        // If dbName is already an absolute path (legacy), use it as-is.
        // Otherwise resolve it under the PharmDesk data directory.
        java.io.File dbFile = new java.io.File(dbName);
        if (dbFile.isAbsolute()) {
            return "jdbc:sqlite:" + dbName;
        }
        return "jdbc:sqlite:" + com.my.pharmacy.util.AppPaths.DB_FILE;
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