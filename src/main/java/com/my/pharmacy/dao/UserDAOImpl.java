package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                    if (BCrypt.checkpw(password, storedHash)) {
                        return new User(rs.getInt("id"), rs.getString("username"), storedHash,
                                rs.getString("role"), rs.getString("full_name"), rs.getInt("is_active") == 1);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role ASC, username ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username"), "",
                        rs.getString("role"), rs.getString("full_name"), rs.getInt("is_active") == 1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    @Override
    public boolean addUser(User user, String plainTextPassword) {
        String sql = "INSERT INTO users (username, password, role, full_name, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, BCrypt.hashpw(plainTextPassword, BCrypt.gensalt())); // HASHING
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getFullName());
            pstmt.setInt(5, user.isActive() ? 1 : 0);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean updatePassword(int userId, String newPlainTextPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, BCrypt.hashpw(newPlainTextPassword, BCrypt.gensalt())); // HASHING
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean toggleUserStatus(int userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isActive ? 1 : 0);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}