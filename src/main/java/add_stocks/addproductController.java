package add_stocks;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import database.database_utility;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

public class addproductController {
    @FXML private Pane addPane;
    @FXML private TextField descriptionField;
    @FXML private TextField volumeField;
    @FXML private TextField categoryField;
    @FXML private TextField salesOfftakeField;
    @FXML private TextField stocksOnHandField;
    @FXML private Button continueButton;
    @FXML private Button cancelButton;

    private dashboard.dashboardController dashboardControllerRef;
    private final List<String> ALL_MONTHS = Arrays.asList("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec");

    @FXML
    private void initialize() {
        if (continueButton != null) {
            continueButton.setOnAction(e -> handleContinue());
        }
        if (cancelButton != null) {
            cancelButton.setOnAction(e -> handleCancel());
        }
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) addPane.getScene().getWindow();
        stage.close();
    }

    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private int getNextItemCode() throws Exception {
        Object[] result = database_utility.query("SELECT COALESCE(MAX(item_code), 0) + 1 as next_code FROM sale_offtake");
        if (result != null) {
            ResultSet rs = (ResultSet) result[1];
            if (rs.next()) {
                return rs.getInt("next_code");
            }
        }
        return 1; // Start with 1 if no existing records
    }

    private void handleContinue() {
        // Validate input fields
        if (!validateFields()) {
            return;
        }

        try {
            // Get values from fields
            String description = descriptionField.getText().trim();
            int volume = Integer.parseInt(volumeField.getText().trim());
            String category = categoryField.getText().trim();
            int salesOfftake = Integer.parseInt(salesOfftakeField.getText().trim());
            int stocksOnHand = Integer.parseInt(stocksOnHandField.getText().trim());

            Connection connect = null;
            try {
                // Get the next available item_code
                int itemCode = getNextItemCode();
                
                // Get the selected month from dashboardController
                String selectedMonth = dashboardControllerRef.getSelectedMonthColumn();

                // Build the columns and values for sale_offtake query
                StringBuilder saleOfftakeColumns = new StringBuilder("item_code, item_description, volume, category");
                StringBuilder saleOfftakeValues = new StringBuilder("?, ?, ?, ?");
                Object[] saleOfftakeParams = new Object[ALL_MONTHS.size() + 4]; // +4 for item_code, description, volume, category
                saleOfftakeParams[0] = itemCode;
                saleOfftakeParams[1] = description;
                saleOfftakeParams[2] = volume;
                saleOfftakeParams[3] = category;

                int paramIndex = 4;
                for (String month : ALL_MONTHS) {
                    saleOfftakeColumns.append(", `").append(month).append("`");
                    saleOfftakeValues.append(", ?");
                    saleOfftakeParams[paramIndex++] = month.equals(selectedMonth) ? salesOfftake : 0;
                }

                // First insert into sale_offtake table
                String saleOfftakeQuery = String.format(
                    "INSERT INTO sale_offtake (%s) VALUES (%s)",
                    saleOfftakeColumns.toString(),
                    saleOfftakeValues.toString()
                );
                
                Object[] result = database_utility.update(saleOfftakeQuery, saleOfftakeParams);
                
                if (result != null) {
                    connect = (Connection) result[0];
                    
                    // Build the columns and values for stock_onhand query
                    StringBuilder stockOnHandColumns = new StringBuilder("item_code");
                    StringBuilder stockOnHandValues = new StringBuilder("?");
                    Object[] stockOnHandParams = new Object[ALL_MONTHS.size() + 1]; // +1 for item_code
                    stockOnHandParams[0] = itemCode;

                    paramIndex = 1;
                    for (String month : ALL_MONTHS) {
                        stockOnHandColumns.append(", `").append(month).append("1`");
                        stockOnHandValues.append(", ?");
                        stockOnHandParams[paramIndex++] = month.equals(selectedMonth) ? stocksOnHand : 0;
                    }

                    // Now insert into stock_onhand table
                    String stockOnHandQuery = String.format(
                        "INSERT INTO stock_onhand (%s) VALUES (%s)",
                        stockOnHandColumns.toString(),
                        stockOnHandValues.toString()
                    );
                    
                    database_utility.update(stockOnHandQuery, stockOnHandParams);
                    
                    showAlert("Success", "Product added successfully.");
                    
                    // Refresh the inventory table
                    if (dashboardControllerRef != null) {
                        dashboardControllerRef.inventory_management_query();
                        // Add recent notification
                        dashboardControllerRef.addRecentStockNotification(stocksOnHand, description);
                    }
                    
                    // Close the form
                    Stage stage = (Stage) continueButton.getScene().getWindow();
                    stage.close();
                }
            } finally {
                if (connect != null) {
                    database_utility.close(connect);
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for Volume, Sales Offtake, and Stocks on Hand.");
        } catch (Exception e) {
            showAlert("Error", "Failed to add product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        if (descriptionField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a product description.");
            return false;
        }
        if (volumeField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a volume.");
            return false;
        }
        if (categoryField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a category.");
            return false;
        }
        if (salesOfftakeField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter sales offtake.");
            return false;
        }
        if (stocksOnHandField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter stocks on hand.");
            return false;
        }
        
        try {
            Integer.parseInt(volumeField.getText().trim());
            Integer.parseInt(salesOfftakeField.getText().trim());
            Integer.parseInt(stocksOnHandField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Volume, Sales Offtake, and Stocks on Hand must be valid numbers.");
            return false;
        }
        
        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setDashboardController(dashboard.dashboardController controller) {
        this.dashboardControllerRef = controller;
    }
} 