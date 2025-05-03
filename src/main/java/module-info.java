module inventory.example.inventory_ms {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires javafx.graphics;
    requires java.sql;

    opens login to javafx.fxml;
    exports login;


}