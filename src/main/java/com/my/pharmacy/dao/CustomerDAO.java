package com.my.pharmacy.dao;

import com.my.pharmacy.model.Customer;
import java.util.List;

public interface CustomerDAO {
    // FIX #4: Returns the generated ID so callers can fetch the saved customer by ID
    // instead of searching by name (which breaks with duplicate names).
    int addCustomer(Customer customer);
    List<Customer> getAllCustomers();
    Customer getCustomerById(int id);
}