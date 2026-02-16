package com.my.pharmacy.dao;

import com.my.pharmacy.model.Sale;

public interface SaleDAO {
    // This method handles the entire transaction:
    // 1. Save Sale Header
    // 2. Save All Items
    // 3. Deduct Stock from Batches
    void saveSale(Sale sale) throws Exception;
}
