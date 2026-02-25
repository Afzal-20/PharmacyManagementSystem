package com.my.pharmacy.dao;

import com.my.pharmacy.model.Batch;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface BatchDAO {
    void addBatch(Batch batch);
    List<Batch> getAllBatches();
    Batch getBatchById(int id);
    List<Batch> getBatchesByProductId(int productId);
    void updateBatch(Batch batch);
    void deleteBatch(int id);
    void reduceStock(Connection conn, int batchId, int qty) throws SQLException;
    void adjustStockWithAudit(int batchId, int oldQty, int newQty, String reason, int userId);

    // --- NEW: Exact Match Duplicate Guard ---
    Batch getExactBatchMatch(int productId, String batchNo, String expiryDate, double costPrice, double tradePrice, double retailPrice);
}