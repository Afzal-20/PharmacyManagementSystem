package com.my.pharmacy;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.config.DatabaseSetup;
import com.my.pharmacy.dao.BatchDAO;
import com.my.pharmacy.dao.CustomerDAO;
import com.my.pharmacy.dao.ProductDAO;
import com.my.pharmacy.dao.SaleDAO;
import com.my.pharmacy.dao.BatchDAOImpl;
import com.my.pharmacy.dao.CustomerDAOImpl;
import com.my.pharmacy.dao.ProductDAOImpl;
import com.my.pharmacy.dao.SaleDAOImpl;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.Customer;
import com.my.pharmacy.model.Product;
import com.my.pharmacy.model.Sale;
import com.my.pharmacy.model.SaleItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class BackendTester {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("🧪 STARTING WHOLESALE SYSTEM DIAGNOSTIC v2");
        System.out.println("==========================================");

        // 1. Initialize
        try {
            DatabaseSetup.initialize(); // Ensure DB exists
        } catch (Exception e) { e.printStackTrace(); }

        // 2. CREATE A CUSTOMER (This fixes the Foreign Key Error)
        System.out.print("Creating Customer... ");
        CustomerDAO customerDAO = new CustomerDAOImpl();
        // Check if customers exist first
        if (customerDAO.getAllCustomers().isEmpty()) {
            Customer c = new Customer("Walk-in Client", "0000", "N/A", "RETAIL");
            customerDAO.addCustomer(c);
            System.out.println("✅ ADDED (ID 1)");
        } else {
            System.out.println("✅ EXISTS (Skipping)");
        }

        // 3. Product & Batch
        ProductDAO productDAO = new ProductDAOImpl();
        Product p = new Product(0, "Test Medicine", "Paracetamol", "GSK", "Painkiller", 10, 5, "A1");
        productDAO.addProduct(p);
        // Get the ID of the product we just made
        List<Product> products = productDAO.getAllProducts();
        int productId = products.get(products.size() - 1).getId();

        BatchDAO batchDAO = new BatchDAOImpl();
        Batch b = new Batch(0, productId, "B-TEST", "2026-12-31", 100, 10.0, 12.0, 15.0, 0.0);
        batchDAO.addBatch(b);
        // Get the ID of the batch we just made
        List<Batch> batches = batchDAO.getAllBatches();
        int batchId = batches.get(batches.size() - 1).getBatchId();

        // 4. Test Sales
        System.out.print("Testing Sales Transaction... ");
        SaleDAO saleDAO = new SaleDAOImpl();
        Sale sale = new Sale();
        sale.setTotalAmount(24.0);
        sale.setPaymentMode("CASH");

        // NOW this works because Customer ID 1 exists!
        sale.setCustomerId(1);
        sale.setSalesmanId(1); // Admin user (ID 1) exists from DatabaseSetup
        sale.setSaleDate(new Timestamp(System.currentTimeMillis()));

        SaleItem item = new SaleItem(productId, batchId, 2, 12.0, 0, 0.0);
        sale.addItem(item);

        try {
            saleDAO.saveSale(sale);
            System.out.println("✅ PASSED (Sale Saved)");
        } catch (Exception e) {
            System.err.println("❌ FAILED: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("==========================================");
        System.out.println("🚀 READY FOR UI INTEGRATION");
        System.out.println("==========================================");
    }
}