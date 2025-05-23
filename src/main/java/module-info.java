module inventory.example.inventory_ms {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires javafx.graphics;
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
}