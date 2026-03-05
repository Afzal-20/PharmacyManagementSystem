package com.my.pharmacy.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseSetup {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable Foreign Keys
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Load schema from file
            InputStream is = DatabaseSetup.class.getResourceAsStream("/database/schema.sql");
            if (is == null) {
                System.err.println("❌ CRITICAL: database/schema.sql not found!");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();

                // Skip empty lines and full-line comments
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                    continue;
                }

                // Append a newline so inline comments (--) don't comment out the rest of the query
                sql.append(line).append("\n");

                // Execute statement when ending with semicolon
                if (trimmedLine.endsWith(";")) {
                    try {
                        stmt.execute(sql.toString());
                    } catch (Exception e) {
                        System.err.println("❌ Error executing SQL: " + sql.toString());
                        throw e; // Rethrow to stop initialization
                    }
                    sql.setLength(0); // Reset buffer
                }
            }

            // --- Secure Admin & Salesman Account Injection (PreparedStatement) ---
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {

                    String insertSql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";

                    try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                        // 1. Add Admin
                        pstmt.setString(1, "admin");
                        pstmt.setString(2, org.mindrot.jbcrypt.BCrypt.hashpw("admin123", org.mindrot.jbcrypt.BCrypt.gensalt()));
                        pstmt.setString(3, "ADMIN");
                        pstmt.setString(4, "System Administrator");
                        pstmt.executeUpdate();

                        // 2. Add Salesman
                        pstmt.setString(1, "salesman");
                        pstmt.setString(2, org.mindrot.jbcrypt.BCrypt.hashpw("sales123", org.mindrot.jbcrypt.BCrypt.gensalt()));
                        pstmt.setString(3, "SALESMAN");
                        pstmt.setString(4, "Counter Salesman");
                        pstmt.executeUpdate();
                    }
                    System.out.println("✅ Default Admin and Salesman accounts created securely using PreparedStatement.");
                }
            }

            System.out.println("✅ Database schema initialized successfully.");

        } catch (Exception e) {
            System.err.println("❌ Database Initialization Failed:");
            e.printStackTrace();
        }
    }
}