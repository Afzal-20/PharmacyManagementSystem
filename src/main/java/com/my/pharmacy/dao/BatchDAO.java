package com.my.pharmacy.dao;

import com.my.pharmacy.model.Batch;
import java.util.List;

public interface BatchDAO {
    // 1. Create (Purchasing new stock)
    void addBatch(Batch batch);

    // 2. Read
    List<Batch> getAllBatches();
    Batch getBatchById(int id);
    List<Batch> getBatchesByProductId(int productId); // Useful for "View History"

    // 3. Update (Fixing pricing or expiry errors)
    void updateBatch(Batch batch);

    // 4. Delete
    void deleteBatch(int id);

    // 5. Transactional (Selling)
    void reduceStock(int batchId, int quantity);
}