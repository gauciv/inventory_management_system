package add_stocks;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import database.database_utility;
import dashboard.Inventory_management_bin;
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
    private Inventory_management_bin itemToEdit;
    private boolean isEditMode = false;

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

                if (isEditMode) {
                    updateExistingProduct(description, volume, category, salesOfftake, stocksOnHand);
                } else {
                    addNewProduct(description, volume, category, salesOfftake, stocksOnHand);
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

    public void setItemToEdit(Inventory_management_bin item) {
        this.itemToEdit = item;
        this.isEditMode = true;
        populateFields();
    }

    private void populateFields() {
        if (itemToEdit != null) {
            descriptionField.setText(itemToEdit.getItem_des());
            volumeField.setText(String.valueOf(itemToEdit.getVolume()));
            categoryField.setText(itemToEdit.getCategory());
            salesOfftakeField.setText(String.valueOf(itemToEdit.getSot()));
            stocksOnHandField.setText(String.valueOf(itemToEdit.getSoh()));
        }
    }

    private void updateExistingProduct(String description, int volume, String category, int salesOfftake, int stocksOnHand) {
        Connection connect = null;
        try {
            String selectedMonth = dashboardControllerRef.getSelectedMonthColumn();
            
            // Update sale_offtake table
            String saleUpdate = String.format(
                "UPDATE sale_offtake SET item_description = ?, volume = ?, category = ?, %s = ? WHERE item_code = ?",
                selectedMonth
            );
            Object[] saleResult = database_utility.update(saleUpdate, 
                description, volume, category, salesOfftake, itemToEdit.getItem_code()
            );

            if (saleResult != null) {
                connect = (Connection) saleResult[0];
                
                // Update stock_onhand table
                String stockUpdate = String.format(
                    "UPDATE stock_onhand SET %s1 = ? WHERE item_code = ?",
                    selectedMonth
                );
                Object[] stockResult = database_utility.update(stockUpdate, 
                    stocksOnHand, itemToEdit.getItem_code()
                );

                if (stockResult != null) {
                    showAlert("Success", "Product updated successfully");
                    if (dashboardControllerRef != null) {
                        dashboardControllerRef.inventory_management_query();
                    }
                    handleCancel();
                } else {
                    showAlert("Error", "Failed to update stock on hand data");
                }
            } else {
                showAlert("Error", "Failed to update product data");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        } finally {
            if (connect != null) {
                database_utility.close(connect);
            }
        }
    }

    private void addNewProduct(String description, int volume, String category, int salesOfftake, int stocksOnHand) {
        Connection connect = null;
        try {
            // Insert into sale_offtake table first
            String insertSaleQuery = "INSERT INTO sale_offtake (item_description, volume, category";
            String values = "VALUES (?, ?, ?";
            for (String month : ALL_MONTHS) {
                insertSaleQuery += ", `" + month + "`";
                values += ", ?";
            }
            insertSaleQuery += ") " + values + ")";

            Object[] params = new Object[ALL_MONTHS.size() + 3];
            params[0] = description;
            params[1] = volume;
            params[2] = category;
            for (int i = 0; i < ALL_MONTHS.size(); i++) {
                params[i + 3] = salesOfftake;
            }

            System.out.println("Executing sale_offtake insert: " + insertSaleQuery);
            Object[] saleResult = database_utility.update(insertSaleQuery, params);
            
            if (saleResult != null) {
                connect = (Connection) saleResult[0];
                
                // Get the most recently inserted item_code
                String getItemCodeQuery = "SELECT item_code FROM sale_offtake WHERE item_description = ? AND volume = ? AND category = ? ORDER BY item_code DESC LIMIT 1";
                Object[] queryParams = new Object[]{description, volume, category};
                Object[] itemCodeResult = database_utility.query(getItemCodeQuery, queryParams);
                
                if (itemCodeResult != null && itemCodeResult.length > 1) {
                    ResultSet rs = (ResultSet) itemCodeResult[1];
                    if (rs.next()) {
                        int newItemCode = rs.getInt("item_code");
                        System.out.println("Retrieved item_code: " + newItemCode);

                        // Now insert into stock_onhand with the correct item_code
                        String insertStockQuery = "INSERT INTO stock_onhand (item_code";
                        values = "VALUES (?";
                        for (String month : ALL_MONTHS) {
                            insertStockQuery += ", `" + month + "1`";
                            values += ", ?";
                        }
                        insertStockQuery += ") " + values + ")";

                        Object[] stockParams = new Object[ALL_MONTHS.size() + 1];
                        stockParams[0] = newItemCode;
                        for (int i = 0; i < ALL_MONTHS.size(); i++) {
                            stockParams[i + 1] = stocksOnHand;
                        }

                        System.out.println("Executing stock_onhand insert: " + insertStockQuery);
                        Object[] stockResult = database_utility.update(insertStockQuery, stockParams);
                        
                        if (stockResult != null) {
                            showAlert("Success", "Product added successfully");
                            if (dashboardControllerRef != null) {
                                dashboardControllerRef.inventory_management_query();
                                // Add notification for new product
                                dashboardControllerRef.addInventoryActionNotification("add", description);
                            }
                            handleCancel();
                        } else {
                            // If stock_onhand insert fails, we should rollback the sale_offtake insert
                            database_utility.update("DELETE FROM sale_offtake WHERE item_code = ?", newItemCode);
                            showAlert("Error", "Failed to add stock on hand data");
                        }
                    } else {
                        showAlert("Error", "Failed to retrieve the new item code");
                    }
                } else {
                    showAlert("Error", "Failed to get item code for new product");
                }
            } else {
                showAlert("Error", "Failed to add product data");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        } finally {
            if (connect != null) {
                database_utility.close(connect);
            }
        }
    }
} 