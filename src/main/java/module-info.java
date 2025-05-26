module inventory.example.inventory_ms {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires java.sql;
    requires mysql.connector.j;

    opens login to javafx.fxml;
    exports login;

    opens dashboard to javafx.fxml;
    exports dashboard;

    exports add_stocks;
    opens add_stocks to javafx.fxml;

    exports sold_stocks;
    opens sold_stocks to javafx.fxml;

    exports confirmation;
    opens confirmation to javafx.fxml;
    
    opens database;

    exports add_edit_product to javafx.fxml;
    opens add_edit_product to javafx.fxml;


}