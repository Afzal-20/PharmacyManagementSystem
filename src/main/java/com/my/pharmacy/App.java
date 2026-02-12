package com.my.pharmacy;

import com.my.pharmacy.config.DatabaseConnection; // Import the class we just made

public class App {
    public static void main(String[] args) {
        System.out.println("Pharmacy App Started...");

        // TEST: Try to connect to the database
        DatabaseConnection.getInstance();
    }
}