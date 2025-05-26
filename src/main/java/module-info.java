module inventory_management_system {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.jetbrains.annotations;
    requires transitive javafx.graphics;
    requires transitive javafx.base;

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