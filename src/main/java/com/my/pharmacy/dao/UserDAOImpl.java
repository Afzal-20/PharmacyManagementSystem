package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl implements UserDAO {

    @Override
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("full_name"),
                            rs.getInt("is_active") == 1
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Login failed
    }
}