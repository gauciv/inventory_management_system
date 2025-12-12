package sold_stocks;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
// TODO: Replace all database/database_utility and SQL logic with Firebase SDK
import javafx.scene.control.ButtonType;

public class soldstocksController {

    @FXML private Pane sold_pane;
    @FXML private TextField volumeField;
    @FXML private TextField categoryField;
    @FXML private TextField salesOfftakeField;
    @FXML private TextField stocksOnHandField;
    @FXML private TextField soldStocksField;
    @FXML private Button continueButton;

    private int itemCode = -1;
    private int currentSoh = 0;
    private dashboard.dashboardController dashboardControllerRef;

    @FXML
    private void Exit() {
        // Get the stage (window) that contains the button
        Stage stage = (Stage) sold_pane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void initialize() {
        if (continueButton != null) {
            continueButton.setOnAction(e -> handleContinue());
        }
    }

    private void handleContinue() {
        if (itemCode == -1) {
            showAlert("Error", "No item selected.");
            return;
        }

        String soldStocksStr = soldStocksField.getText();
        int soldStocks;
        try {
            soldStocks = Integer.parseInt(soldStocksStr);
        } catch (NumberFormatException ex) {
            showAlert("Input Error", "Please enter a valid number for stocks sold.");
            return;
        }

        if (soldStocks > currentSoh) {
            showAlert("Error", "Not enough stocks available. Current stock on hand: " + currentSoh);
            return;
        }

        int updatedSoh = currentSoh - soldStocks;
        // TODO: Replace with Firebase update logic
        System.out.println("TODO: Update sold stocks and sales data in Firebase");
        if (dashboardControllerRef != null) {
            dashboardControllerRef.addSoldStockNotification(soldStocks, volumeField.getText() + "mL");
            dashboardControllerRef.inventory_management_query();
        }
        showAlert("Success", "Stock sold successfully and sales data updated (Firebase TODO)");
        Stage stage = (Stage) sold_pane.getScene().getWindow();
        stage.close();
    }

    public void setSelectedItemDescription(String description) {
        // Implement if needed to show item description
    }

    public void setItemData(int itemCode, int volume, String category, int salesOfftake, int stocksOnHand) {
        this.itemCode = itemCode;
        this.currentSoh = stocksOnHand;

        volumeField.setText(String.valueOf(volume));
        categoryField.setText(category);
        salesOfftakeField.setText(String.valueOf(salesOfftake));
        stocksOnHandField.setText(String.valueOf(stocksOnHand));
    }

    public void setDashboardController(dashboard.dashboardController controller) {
        this.dashboardControllerRef = controller;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
        
        if (title.equals("Success")) {
            // Apply custom styling for success alerts
            alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/success-alert.css").toExternalForm()
            );
            alert.initStyle(StageStyle.TRANSPARENT);
            Scene scene = alert.getDialogPane().getScene();
            scene.setFill(null);
            
            // Remove the graphic
            alert.getDialogPane().setGraphic(null);
        }
        
        alert.showAndWait();
    }
}

