module com.my.pharmacy {
    // 1. UI Modules
    requires javafx.controls;
    requires javafx.fxml;

    // 2. Database Modules
    requires java.sql;        // Core SQL API
    requires org.xerial.sqlitejdbc; // SQLite Driver

    // 3. Printing/PDF Modules
    requires layout;          // iText PDF Layout
    requires kernel;          // iText PDF Kernel

    // 4. Permissions (Opening packages to JavaFX)
    opens com.my.pharmacy to javafx.fxml;
    exports com.my.pharmacy;

    // Allow JavaFX to load our Controllers
    opens com.my.pharmacy.controller to javafx.fxml;
    exports com.my.pharmacy.controller;

    // Allow Database to see our Models (for saving data)
    opens com.my.pharmacy.model to javafx.base;
    exports com.my.pharmacy.model;
}