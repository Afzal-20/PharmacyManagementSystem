package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImpl implements CustomerDAO {

    private static final Logger log = LoggerFactory.getLogger(CustomerDAOImpl.class);

    @Override
    public int addCustomer(Customer customer) {
        // current_balance column removed — balance is tracked via the payments ledger
        String sql = "INSERT INTO customers (name, phone, address, type, cnic) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getAddress());
            pstmt.setString(4, customer.getType());
            pstmt.setString(5, customer.getCnic());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Failed to add customer: {}", e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to load customers: {}", e.getMessage(), e);
        }
        return customers;
    }

    @Override
    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            log.error("Failed to get customer by id {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void updateCustomer(Customer customer) {
        if (customer.getId() == 1) return; // walk-in placeholder is system-protected
        String sql = "UPDATE customers SET name=?, phone=?, address=?, cnic=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getAddress());
            pstmt.setString(4, customer.getCnic());
            pstmt.setInt(5, customer.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update customer {}: {}", customer.getId(), e.getMessage(), e);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("type"),
                rs.getString("cnic")
        );
    }
}