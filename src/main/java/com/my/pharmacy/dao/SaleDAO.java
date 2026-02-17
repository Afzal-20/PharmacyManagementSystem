package com.my.pharmacy.dao;

import com.my.pharmacy.model.Sale;
import java.util.List;

public interface SaleDAO {
    // 1. Transaction
    void saveSale(Sale sale);

    // 2. Reporting
    List<Sale> getAllSales();
    Sale getSaleById(int id); // For "Reprint Invoice"
}