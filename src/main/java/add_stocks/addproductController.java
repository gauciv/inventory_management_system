package add_stocks;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.ButtonType;
import javafx.scene.Scene;
import java.util.Arrays;
import java.util.List;
import firebase.FirestoreClient;
import firebase.FirebaseConfig;
import org.json.JSONObject;
import dashboard.Inventory_management_bin;
import dashboard.dashboardController;

public class addproductController {
    @FXML private Pane addPane;
    @FXML private TextField descriptionField;
    @FXML private TextField volumeField;
    @FXML private TextField categoryField;
    @FXML private TextField salesOfftakeField;
    @FXML private TextField stocksOnHandField;
    @FXML private Button continueButton;
    @FXML private Button cancelButton;

    private dashboardController dashboardControllerRef;
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

    public void setDashboardController(dashboardController controller) {
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

    private int getNextItemCode() {
        return (int) (Math.random() * 100000); 
    }

    private void handleContinue() {
        if (!validateFields()) return;

        // Fetch ID directly to avoid null issues
        String projectId = FirebaseConfig.getProjectId();
        if (projectId == null) {
            showAlert("Error", "Project ID not found. Check serviceAccountKey.json");
            return;
        }

        // Get auth token from dashboard if needed
        if (idToken == null && dashboardControllerRef != null) {
            idToken = dashboardControllerRef.getIdToken();
        }

        String description = descriptionField.getText().trim();
        int volume = Integer.parseInt(volumeField.getText().trim());
        String category = categoryField.getText().trim();
        int salesOfftake = Integer.parseInt(salesOfftakeField.getText().trim());
        int stocksOnHand = Integer.parseInt(stocksOnHandField.getText().trim());

        if (isEditMode) {
            updateExistingProduct(projectId, description, volume, category, salesOfftake, stocksOnHand);
        } else {
            addNewProduct(projectId, description, volume, category, salesOfftake, stocksOnHand);
        }
    }

    private void addNewProduct(String projectId, String description, int volume, String category, int salesOfftake, int stocksOnHand) {
        System.out.println("--- ADDING PRODUCT DEBUG ---");
        System.out.println("Target Project: " + projectId);
        
        // Generate code once so we use it for both the field and the doc ID
        int newItemCode = getNextItemCode();

        new Thread(() -> {
            try {
                // Construct JSON payload
                JSONObject fields = new JSONObject();
                fields.put("item_code", new JSONObject().put("integerValue", newItemCode));
                fields.put("item_des", new JSONObject().put("stringValue", description));
                fields.put("volume", new JSONObject().put("integerValue", volume));
                fields.put("category", new JSONObject().put("stringValue", category));
                fields.put("sot", new JSONObject().put("integerValue", salesOfftake));
                fields.put("soh", new JSONObject().put("integerValue", stocksOnHand));
                
                JSONObject doc = new JSONObject();
                doc.put("fields", fields);
                
                // FIX: Use the Item Code as the Document ID (inventory/12345)
                // This ensures we can find it later to update it.
                // We use setDocument (which uses PATCH+Override) to create/overwrite this specific ID.
                String response = FirestoreClient.setDocument(projectId, "inventory/" + newItemCode, idToken, doc.toString());
                
                System.out.println("Write Response: " + response);

                javafx.application.Platform.runLater(() -> {
                    showAlert("Success", "Product added.");
                    if (dashboardControllerRef != null) {
                        dashboardControllerRef.inventory_management_query();
                        dashboardControllerRef.addInventoryActionNotification("add", description);
                    }
                    handleCancel();
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> showAlert("Error", "Add failed: " + e.getMessage()));
            }
        }).start();
    }

    private void updateExistingProduct(String projectId, String description, int volume, String category, int salesOfftake, int stocksOnHand) {
        new Thread(() -> {
            try {
                // This will now match the ID we created in addNewProduct
                String docId = String.valueOf(itemToEdit.getItem_code());
                
                JSONObject fields = new JSONObject();
                fields.put("item_code", new JSONObject().put("integerValue", itemToEdit.getItem_code()));
                fields.put("item_des", new JSONObject().put("stringValue", description));
                fields.put("volume", new JSONObject().put("integerValue", volume));
                fields.put("category", new JSONObject().put("stringValue", category));
                fields.put("sot", new JSONObject().put("integerValue", salesOfftake));
                fields.put("soh", new JSONObject().put("integerValue", stocksOnHand));
                
                JSONObject doc = new JSONObject();
                doc.put("fields", fields);

                // Update the specific document
                FirestoreClient.setDocument(projectId, "inventory/" + docId, idToken, doc.toString());

                javafx.application.Platform.runLater(() -> {
                    showAlert("Success", "Product updated.");
                    if (dashboardControllerRef != null) dashboardControllerRef.inventory_management_query();
                    handleCancel();
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> showAlert("Error", "Update failed: " + e.getMessage()));
            }
        }).start();
    }

    private boolean validateFields() {
        if (descriptionField.getText().trim().isEmpty() || volumeField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "All fields are required.");
            return false;
        }
        try {
            Integer.parseInt(volumeField.getText().trim());
            Integer.parseInt(salesOfftakeField.getText().trim());
            Integer.parseInt(stocksOnHandField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid numbers.");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.NONE);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
        alert.showAndWait();
    }
}