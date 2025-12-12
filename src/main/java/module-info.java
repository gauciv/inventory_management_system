module inventory.example.inventory_ms {
    // JavaFX Modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    
    // UI Library Modules
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    
    // Utilities
    requires org.jetbrains.annotations;
    requires org.json;
    requires java.sql;
    
    // --- FIX: Google Auth for Firebase ---
    requires com.google.auth.oauth2;
    requires com.google.auth;
    // ------------------------------------

    // Open packages to JavaFX reflection
    opens login to javafx.fxml, javafx.base, javafx.controls, javafx.graphics;
    opens dashboard to javafx.fxml, javafx.base, javafx.controls, javafx.graphics;
    opens forecasting to javafx.fxml, javafx.base, javafx.controls, javafx.graphics;
    opens confirmation to javafx.fxml, javafx.base, javafx.controls, javafx.graphics;
    opens sold_stocks to javafx.fxml, javafx.base, javafx.controls, javafx.graphics;
    opens add_stocks to javafx.fxml, javafx.base, javafx.controls, javafx.graphics;
    opens add_edit_product to javafx.fxml, javafx.base, javafx.controls, javafx.graphics;
    
    // Export packages
    exports login;
    exports dashboard;
    exports forecasting;
    exports confirmation;
    exports sold_stocks;
    exports add_stocks;
    exports add_edit_product;
}