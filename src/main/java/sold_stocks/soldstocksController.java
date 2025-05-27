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

        // Check if there are enough stocks to sell
        if (soldStocks > currentSoh) {
            showAlert("Error", "Not enough stocks available. Current stock on hand: " + currentSoh);
            return;
        }

        // Calculate new stock on hand
        int updatedSoh = currentSoh - soldStocks;

        // Update stock_onhand table
        Connection connect = null;
        try {
            Object[] result = database_utility.update(
                "UPDATE stock_onhand SET dec1 = ? WHERE item_code = ?",
                updatedSoh, itemCode
            );
            if (result != null) {
                connect = (Connection) result[0];
                // Update sale_offtake table
                database_utility.update(
                    "UPDATE sale_offtake SET dec = dec + ? WHERE item_code = ?",
                    soldStocks, itemCode
                );
            }
            showAlert("Success", "Stock sold successfully.");
            
            // Close the form
            Stage stage = (Stage) sold_pane.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            showAlert("Database Error", "Failed to update stock: " + e.getMessage());
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

