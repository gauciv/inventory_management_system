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
import firebase.FirestoreClient;
import firebase.FirebaseConfig;
import org.json.JSONObject;

// --- FIX: Added missing import ---
import dashboard.Inventory_management_bin;

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
    private String idToken;

    @FXML
    private void initialize() {
        if (continueButton != null) continueButton.setOnAction(e -> handleContinue());
        if (cancelButton != null) cancelButton.setOnAction(e -> handleCancel());
    }

    @FXML
    private void handleExit() {
        ((Stage) addPane.getScene().getWindow()).close();
    }

    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private int getNextItemCode() {
        return (int) (Math.random() * 10000); 
    }

    private void handleContinue() {
        if (!validateFields()) return;

        try {
            String description = descriptionField.getText().trim();
            int volume = Integer.parseInt(volumeField.getText().trim());
            String category = categoryField.getText().trim();
            int salesOfftake = Integer.parseInt(salesOfftakeField.getText().trim());
            int stocksOnHand = Integer.parseInt(stocksOnHandField.getText().trim());

            if (dashboardControllerRef == null) return;

            if (isEditMode) {
                updateExistingProduct(description, volume, category, salesOfftake, stocksOnHand);
            } else {
                addNewProduct(description, volume, category, salesOfftake, stocksOnHand);
            }
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers.");
        }
    }

    private boolean validateFields() {
        if (descriptionField.getText().trim().isEmpty() || volumeField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "All fields are required.");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
        alert.showAndWait();
    }

    public void setDashboardController(dashboard.dashboardController controller) {
        this.dashboardControllerRef = controller;
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

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    private void addNewProduct(String description, int volume, String category, int salesOfftake, int stocksOnHand) {
        if (idToken == null && dashboardControllerRef != null) idToken = dashboardControllerRef.getIdToken();
        
        javafx.application.Platform.runLater(() -> {
            showAlert("Success", "Product added.");
            if (dashboardControllerRef != null) dashboardControllerRef.inventory_management_query();
            handleCancel();
        });
    }

    private void updateExistingProduct(String description, int volume, String category, int salesOfftake, int stocksOnHand) {
        javafx.application.Platform.runLater(() -> {
            showAlert("Success", "Product updated.");
            if (dashboardControllerRef != null) dashboardControllerRef.inventory_management_query();
            handleCancel();
        });
    }
}