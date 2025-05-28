package add_stocks;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import dashboard.dashboardController;

public class addstocksController {
    @FXML
    private Pane addPane;
    @FXML @SuppressWarnings("all")
    public TextField text_field1;
    @FXML
    public TextField textfield2;
    @FXML @SuppressWarnings("all")
    public TextField text_field3;
    @FXML @SuppressWarnings("all")
    public TextField text_field4;
    
    private dashboardController dashboardControllerRef;
    private String selectedItemDescription;
    private int itemCode;
    private int currentSoh;

    @FXML
    private void handleExit() {
        // Get the window/stage the button is in and close it
        Stage stage = (Stage) addPane.getScene().getWindow();
        stage.close();
    }
    
    public void setSelectedItemDescription(String description) {
        this.selectedItemDescription = description;
    }
    
    public void setItemCodeAndSoh(int itemCode, int soh) {
        this.itemCode = itemCode;
        this.currentSoh = soh;
    }
    
    public void setDashboardController(dashboardController controller) {
        this.dashboardControllerRef = controller;
    }
}
