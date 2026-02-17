package com.my.pharmacy.dao;

import com.my.pharmacy.model.Customer;
import java.util.List;

public interface CustomerDAO {
    void addCustomer(Customer customer);
    List<Customer> getAllCustomers();
    Customer getCustomerById(int id);
}