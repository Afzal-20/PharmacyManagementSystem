package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.Batch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;

public class BatchDAOImpl implements BatchDAO {

    public BatchDAOImpl() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        // Defines the 'batches' table with a Foreign Key linking to 'products'
        String sql = """
                CREATE TABLE IF NOT EXISTS batches (
                    batch_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_id INTEGER NOT NULL,
                    batch_no TEXT NOT NULL,
                    expiry_date TEXT NOT NULL,
                    purchase_price REAL,
                    sale_price REAL,
                    qty_on_hand INTEGER DEFAULT 0,
                    FOREIGN KEY(product_id) REFERENCES products(id)
                );
                """;

        try (Statement stmt = DatabaseConnection.getInstance().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addBatch(Batch b) {
        String sql = "INSERT INTO batches(product_id, batch_no, expiry_date, purchase_price, sale_price, qty_on_hand) VALUES(?,?,?,?,?,?)";

        try (PreparedStatement pstmt = DatabaseConnection.getInstance().prepareStatement(sql)) {

            // NOTE: We access properties directly to ensure we get the latest value
            pstmt.setInt(1, b.productIdProperty().get());
            pstmt.setString(2, b.getBatchNo());
            pstmt.setString(3, b.getExpiryDate().toString()); // Save Date as String "YYYY-MM-DD"
            pstmt.setDouble(4, b.purchasePriceProperty().get());
            pstmt.setDouble(5, b.salePriceProperty().get());
            pstmt.setInt(6, b.getQtyOnHand());

            pstmt.executeUpdate();
            System.out.println("✅ Batch Saved: " + b.getBatchNo());

        } catch (SQLException e) {
            System.err.println("Error saving batch: " + e.getMessage());
        }
    }

    @Override
    public ObservableList<Batch> getBatchesByProductId(int productId) {
        ObservableList<Batch> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM batches WHERE product_id = ?";

        try (PreparedStatement pstmt = DatabaseConnection.getInstance().prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToBatch(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void updateBatchStock(int batchId, int newQuantity) {
        String sql = "UPDATE batches SET qty_on_hand = ? WHERE batch_id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getInstance().prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, batchId);
            pstmt.executeUpdate();
            System.out.println("✅ Stock Updated for Batch ID: " + batchId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ObservableList<Batch> getAllBatches() {
        ObservableList<Batch> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM batches";

        try (Statement stmt = DatabaseConnection.getInstance().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToBatch(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Helper to convert Database Row -> Java Object
    private Batch mapResultSetToBatch(ResultSet rs) throws SQLException {
        return new Batch(
                rs.getInt("batch_id"),
                rs.getInt("product_id"),
                rs.getString("batch_no"),
                LocalDate.parse(rs.getString("expiry_date")), // Convert String "2026-12-31" -> LocalDate
                rs.getDouble("purchase_price"),
                rs.getDouble("sale_price"),
                rs.getInt("qty_on_hand")
        );
    }
}