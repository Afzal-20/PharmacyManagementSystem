package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserDAOImpl implements UserDAO {
    @Override
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    // Verify password against BCrypt hash
                    if (BCrypt.checkpw(password, storedHash)) {
                        return new User(
                                rs.getInt("id"), rs.getString("username"), storedHash,
                                rs.getString("role"), rs.getString("full_name"), rs.getInt("is_active") == 1
                        );
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}