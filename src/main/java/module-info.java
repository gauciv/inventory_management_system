module inventory.example.inventory_ms {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires java.sql;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.jetbrains.annotations;

    opens login to javafx.fxml, javafx.base, javafx.controls;
    opens dashboard to javafx.fxml, javafx.base, javafx.controls;
    opens forecasting to javafx.fxml, javafx.base, javafx.controls;
    opens confirmation to javafx.fxml, javafx.base, javafx.controls;
    opens sold_stocks to javafx.fxml, javafx.base, javafx.controls;
    opens add_stocks to javafx.fxml, javafx.base, javafx.controls;
    opens add_edit_product to javafx.fxml, javafx.base, javafx.controls;
    opens database to javafx.fxml, javafx.base, javafx.controls;
    
    exports login;
    exports dashboard;
    exports forecasting;
    exports confirmation;
    exports sold_stocks;
    exports add_stocks;
    exports add_edit_product;
    exports database;
}
