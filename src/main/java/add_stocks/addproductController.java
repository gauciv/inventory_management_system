package add_stocks;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import java.util.Arrays;
import java.util.List;
import javafx.scene.control.ButtonType;

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
        // TODO: Replace with Firebase logic to get next item code
        System.out.println("TODO: Get next item code from Firebase");
        return 1; // Placeholder
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

            // Get the selected month from dashboardController
            String selectedMonth = dashboardControllerRef.getSelectedMonthColumn();
            System.out.println("Selected month: " + selectedMonth);

            if (isEditMode) {
                updateExistingProduct(description, volume, category, salesOfftake, stocksOnHand);
            } else {
                addNewProduct(description, volume, category, salesOfftake, stocksOnHand);
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
        // TODO: updateExistingProduct - Replace with Firebase update logic
        System.out.println("TODO: Update existing product in Firebase");
        showAlert("Success", "Product updated successfully (Firebase TODO)");
        if (dashboardControllerRef != null) {
            dashboardControllerRef.inventory_management_query();
        }
        handleCancel();
    }

    private void addNewProduct(String description, int volume, String category, int salesOfftake, int stocksOnHand) {
        // TODO: addNewProduct - Replace with Firebase insert logic
        System.out.println("TODO: Add new product to Firebase");
        showAlert("Success", "Product added successfully (Firebase TODO)");
        if (dashboardControllerRef != null) {
            dashboardControllerRef.inventory_management_query();
            dashboardControllerRef.addInventoryActionNotification("add", description);
        }
        handleCancel();
    }
}