module inventory.example.inventory_ms {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.desktop;
    requires transitive java.sql;
    requires org.jetbrains.annotations;

    opens dashboard to javafx.fxml, javafx.base;
    opens login to javafx.fxml;
    opens add_edit_product to javafx.fxml;
    opens add_stocks to javafx.fxml;
    opens confirmation to javafx.fxml;
    opens sold_stocks to javafx.fxml;
    
    exports dashboard;
    exports login;
    exports add_edit_product;
    exports add_stocks;
    exports confirmation;
    exports sold_stocks;
    exports database;
}
