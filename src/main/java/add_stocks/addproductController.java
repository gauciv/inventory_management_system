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
        System.out.println("Initializing addproductController...");
        if (continueButton != null) {
            continueButton.setOnAction(e -> handleContinue());
            System.out.println("Continue button handler set");
        } else {
            System.out.println("Warning: Continue button is null");
        }
        if (cancelButton != null) {
            cancelButton.setOnAction(e -> handleCancel());
            System.out.println("Cancel button handler set");
        } else {
            System.out.println("Warning: Cancel button is null");
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
        System.out.println("Getting next item code...");
        Object[] result = database_utility.query("SELECT COALESCE(MAX(item_code), 0) + 1 as next_code FROM sale_offtake");
        if (result != null) {
            ResultSet rs = (ResultSet) result[1];
            if (rs.next()) {
                int nextCode = rs.getInt("next_code");
                System.out.println("Next item code: " + nextCode);
                return nextCode;
            }
        }
        System.out.println("No existing records, starting with code 1");
        return 1;
    }

    private void handleContinue() {
        System.out.println("Handle continue called...");
        
        // Validate input fields
        if (!validateFields()) {
            System.out.println("Field validation failed");
            return;
        }

        try {
            // Get values from fields
            String description = descriptionField.getText().trim();
            int volume = Integer.parseInt(volumeField.getText().trim());
            String category = categoryField.getText().trim();
            int salesOfftake = Integer.parseInt(salesOfftakeField.getText().trim());
            int stocksOnHand = Integer.parseInt(stocksOnHandField.getText().trim());

            System.out.println("Input values validated:");
            System.out.println("Description: " + description);
            System.out.println("Volume: " + volume);
            System.out.println("Category: " + category);
            System.out.println("Sales Offtake: " + salesOfftake);
            System.out.println("Stocks on Hand: " + stocksOnHand);

            if (dashboardControllerRef == null) {
                System.out.println("Error: Dashboard controller reference is null!");
                showAlert("Error", "Internal error: Dashboard controller reference is missing");
                return;
            }

            Connection connect = null;
            try {
                // Get the selected month from dashboardController
                String selectedMonth = dashboardControllerRef.getSelectedMonthColumn();
                System.out.println("Selected month: " + selectedMonth);

                // First insert into sale_offtake table
                StringBuilder saleOfftakeColumns = new StringBuilder("item_description, volume, category");
                StringBuilder saleOfftakeValues = new StringBuilder("?, ?, ?");
                Object[] saleOfftakeParams = new Object[ALL_MONTHS.size() + 3];
                saleOfftakeParams[0] = description;
                saleOfftakeParams[1] = volume;
                saleOfftakeParams[2] = category;

                int paramIndex = 3;
                for (String month : ALL_MONTHS) {
                    saleOfftakeColumns.append(", `").append(month).append("`");
                    saleOfftakeValues.append(", ?");
                    saleOfftakeParams[paramIndex++] = month.equals(selectedMonth) ? salesOfftake : 0;
                }

                String saleOfftakeQuery = String.format(
                    "INSERT INTO sale_offtake (%s) VALUES (%s)",
                    saleOfftakeColumns.toString(),
                    saleOfftakeValues.toString()
                );
                
                System.out.println("Executing sale_offtake query: " + saleOfftakeQuery);
                Object[] result = database_utility.update(saleOfftakeQuery, saleOfftakeParams);
                
                if (result != null) {
                    connect = (Connection) result[0];
                    System.out.println("Sale offtake insert successful");
                    
                    // Get the auto-generated item_code using PreparedStatement
                    String getItemCodeQuery = "SELECT item_code FROM sale_offtake WHERE item_description = ? AND volume = ? AND category = ? ORDER BY item_code DESC LIMIT 1";
                    Object[] queryParams = new Object[]{description, volume, category};
                    Object[] itemCodeResult = database_utility.query(getItemCodeQuery, queryParams);
                    
                    if (itemCodeResult != null) {
                        ResultSet rs = (ResultSet) itemCodeResult[1];
                        if (rs.next()) {
                            int itemCode = rs.getInt("item_code");
                            System.out.println("Retrieved auto-generated item_code: " + itemCode);
                            
                            // Now insert into stock_onhand table with the retrieved item_code
                            StringBuilder stockOnHandColumns = new StringBuilder("item_code");
                            StringBuilder stockOnHandValues = new StringBuilder("?");
                            Object[] stockOnHandParams = new Object[ALL_MONTHS.size() + 1];
                            stockOnHandParams[0] = itemCode;

                            paramIndex = 1;
                            for (String month : ALL_MONTHS) {
                                stockOnHandColumns.append(", `").append(month).append("1`");
                                stockOnHandValues.append(", ?");
                                stockOnHandParams[paramIndex++] = month.equals(selectedMonth) ? stocksOnHand : 0;
                            }

                            String stockOnHandQuery = String.format(
                                "INSERT INTO stock_onhand (%s) VALUES (%s)",
                                stockOnHandColumns.toString(),
                                stockOnHandValues.toString()
                            );
                            
                            System.out.println("Executing stock_onhand query: " + stockOnHandQuery);
                            Object[] stockResult = database_utility.update(stockOnHandQuery, stockOnHandParams);
                            
                            if (stockResult != null) {
                                System.out.println("Stock onhand insert successful");
                                showAlert("Success", "Product added successfully.");
                                
                                // Refresh the inventory table
                                if (dashboardControllerRef != null) {
                                    System.out.println("Refreshing inventory table...");
                                    dashboardControllerRef.inventory_management_query();
                                    // Add recent notification
                                    System.out.println("Adding notification...");
                                    dashboardControllerRef.addRecentStockNotification(stocksOnHand, description);
                                }
                                
                                // Close the form
                                Stage stage = (Stage) continueButton.getScene().getWindow();
                                stage.close();
                            } else {
                                System.out.println("Error: Stock onhand insert failed");
                                showAlert("Error", "Failed to add stock on hand data");
                            }
                        } else {
                            System.out.println("Error: Could not retrieve auto-generated item_code");
                            showAlert("Error", "Failed to get item code for new product");
                        }
                    } else {
                        System.out.println("Error: Failed to execute item code query");
                        showAlert("Error", "Failed to retrieve item code");
                    }
                } else {
                    System.out.println("Error: Sale offtake insert failed");
                    showAlert("Error", "Failed to add sales offtake data");
                }
            } finally {
                if (connect != null) {
                    database_utility.close(connect);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Number format error: " + e.getMessage());
            showAlert("Input Error", "Please enter valid numbers for Volume, Sales Offtake, and Stocks on Hand.");
        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to add product: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        System.out.println("Validating fields...");
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
        
        System.out.println("Field validation successful");
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
        System.out.println("Setting dashboard controller reference...");
        this.dashboardControllerRef = controller;
        if (controller != null) {
            System.out.println("Dashboard controller reference set successfully");
        } else {
            System.out.println("Warning: Null dashboard controller reference");
        }
    }
} 