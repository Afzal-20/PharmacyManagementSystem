package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.Dealer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DealerDAOImpl implements DealerDAO {

    @Override
    public void addDealer(Dealer d) {
        String sql = "INSERT INTO dealers (name, company_name, phone, address, license_no) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, d.getName());
            pstmt.setString(2, d.getCompanyName());
            pstmt.setString(3, d.getPhone());
            pstmt.setString(4, d.getAddress());
            pstmt.setString(5, d.getLicenseNo());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<Dealer> getAllDealers() {
        List<Dealer> dealers = new ArrayList<>();
        String sql = "SELECT * FROM dealers";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dealers.add(new Dealer(
                        rs.getInt("id"), rs.getString("name"), rs.getString("company_name"),
                        rs.getString("phone"), rs.getString("address"), rs.getString("license_no")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return dealers;
    }

    @Override
    public Dealer getDealerById(int id) {
        String sql = "SELECT * FROM dealers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Dealer(
                            rs.getInt("id"), rs.getString("name"), rs.getString("company_name"),
                            rs.getString("phone"), rs.getString("address"), rs.getString("license_no")
                    );
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override public void updateDealer(Dealer d) { /* Implementation similar to Customer */ }
    @Override public void deleteDealer(int id) { /* Implementation similar to Customer */ }
}