package com.my.pharmacy.dao;

import com.my.pharmacy.model.Batch;
import javafx.collections.ObservableList;

public interface BatchDAO {
    // Save a new batch (e.g., when buying stock)
    void addBatch(Batch batch);

    // Update stock quantity (e.g., after a sale)
    void updateBatchStock(int batchId, int newQuantity);

    // Get all batches for a specific product (e.g., checking stock for "Panadol")
    ObservableList<Batch> getBatchesByProductId(int productId);

    // Get all batches (e.g., for the "Inventory" screen)
    ObservableList<Batch> getAllBatches();
}