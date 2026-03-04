package com.my.pharmacy.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
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

                // FIX: Append a newline so inline comments (--) don't comment out the rest of the query
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

            // --- NEW: Secure Admin Account Injection ---
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw("admin123", org.mindrot.jbcrypt.BCrypt.gensalt());
                    String insertAdmin = "INSERT INTO users (username, password, role, full_name) VALUES ('admin', '" + hashedPassword + "', 'ADMIN', 'System Administrator')";
                    stmt.execute(insertAdmin);
                    System.out.println("✅ Default Admin account created with BCrypt hashing.");
                }
            }

            // --- Inject Default Salesman Account for Testing ---
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'salesman'")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String hashedSalesman = org.mindrot.jbcrypt.BCrypt.hashpw("sales123", org.mindrot.jbcrypt.BCrypt.gensalt());
                    String insertSalesman = "INSERT INTO users (username, password, role, full_name) VALUES ('salesman', '" + hashedSalesman + "', 'SALESMAN', 'Counter Salesman')";
                    stmt.execute(insertSalesman);
                    System.out.println("✅ Default Salesman account created.");
                }
            }

            System.out.println("✅ Database schema initialized successfully.");

        } catch (Exception e) {
            System.err.println("❌ Database Initialization Failed:");
            e.printStackTrace();
        }
    }
}