package sold_stocks;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import database.database_utility;
import java.sql.Connection;

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
        Connection connect = null;
        try {
            // Get the selected month from dashboardController
            String selectedMonth = dashboardControllerRef.getSelectedMonthColumn();
            
            // Update stock_onhand table with the correct month column
            Object[] result = database_utility.update(
                String.format("UPDATE stock_onhand SET %s1 = ? WHERE item_code = ?", selectedMonth),
                updatedSoh, itemCode
            );
            
            if (result != null) {
                connect = (Connection) result[0];
                
                // Update sale_offtake table with the correct month column
                database_utility.update(
                    String.format("UPDATE sale_offtake SET %s = COALESCE(%s, 0) + ? WHERE item_code = ?", 
                        selectedMonth, selectedMonth),
                    soldStocks, itemCode
                );
                
                // Add notification to dashboard
                if (dashboardControllerRef != null) {
                    dashboardControllerRef.addSoldStockNotification(soldStocks, volumeField.getText() + "mL");
                }
                
                showAlert("Success", "Stock sold successfully and sales data updated.");
                
                // Refresh the inventory table
                if (dashboardControllerRef != null) {
                    dashboardControllerRef.inventory_management_query();
                }
                
                // Close the form
                Stage stage = (Stage) sold_pane.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            showAlert("Database Error", "Failed to update stock and sales data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connect != null) {
                database_utility.close(connect);
            }
        }
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
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

