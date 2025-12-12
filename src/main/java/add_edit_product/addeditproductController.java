package add_edit_product;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
// TODO: Replace all database/database_utility and SQL logic with Firebase SDK
import dashboard.Inventory_management_bin;
import java.sql.Connection;
import java.sql.ResultSet;
import javafx.scene.control.ButtonType;

public class addeditproductController {
    @FXML private Pane addedit_pane;
    @FXML private TextField descriptionField;
    @FXML private TextField volumeField;
    @FXML private TextField categoryField;
    @FXML private TextField salesOfftakeField;
    @FXML private TextField stocksOnHandField;
    @FXML private Button continueButton;
    @FXML private Button cancelButton;
    @FXML private Button closeButton;

    private dashboard.dashboardController dashboardControllerRef;
    private Inventory_management_bin itemToEdit;
    private String currentMonth;

    @FXML
    private void initialize() {
        setupDragPane();
        if (continueButton != null) {
            continueButton.setOnAction(e -> handleContinue());
        }
        if (cancelButton != null) {
            cancelButton.setOnAction(e -> handleCancel());
        }
        if (closeButton != null) {
            closeButton.setOnAction(e -> handleCancel());
        }
    }

    private void setupDragPane() {
        final double[] xOffset = {0};
        final double[] yOffset = {0};

        addedit_pane.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });

        addedit_pane.setOnMouseDragged(event -> {
            Stage stage = (Stage) addedit_pane.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset[0]);
            stage.setY(event.getScreenY() - yOffset[0]);
        });
    }

    public void setDashboardController(dashboard.dashboardController controller) {
        this.dashboardControllerRef = controller;
        this.currentMonth = controller.getSelectedMonthColumn();
    }

    public void setItemToEdit(Inventory_management_bin item) {
        this.itemToEdit = item;
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

    private void handleContinue() {
        try {
            // Validate input
            String description = descriptionField.getText().trim();
            int volume = Integer.parseInt(volumeField.getText().trim());
            String category = categoryField.getText().trim();
            int salesOfftake = Integer.parseInt(salesOfftakeField.getText().trim());
            int stocksOnHand = Integer.parseInt(stocksOnHandField.getText().trim());

            if (description.isEmpty() || category.isEmpty()) {
                showAlert("Error", "All fields must be filled out");
                return;
            }

            if (volume <= 0 || salesOfftake < 0 || stocksOnHand < 0) {
                showAlert("Error", "Volume must be positive, and sales/stocks cannot be negative");
                return;
            }

            // TODO: updateDatabaseRecords - Replace with Firebase update logic
            // Placeholder for Firebase implementation
            System.out.println("TODO: Update product and stock data in Firebase");
            showAlert("Success", "Product updated successfully (Firebase TODO)");
            if (dashboardControllerRef != null) {
                dashboardControllerRef.addInventoryActionNotification("edit", description);
                dashboardControllerRef.inventory_management_query();
            }
            Stage stage = (Stage) continueButton.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for Volume, Sales Offtake, and Stocks on Hand");
        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Removed updateDatabaseRecords method as it is replaced with Firebase logic

    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
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
