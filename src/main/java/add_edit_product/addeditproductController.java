package add_edit_product;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;
import javafx.scene.Scene;
import javafx.stage.StageStyle;
import firebase.FirestoreClient;
import firebase.FirebaseConfig;
import org.json.JSONObject;
import dashboard.Inventory_management_bin;
import dashboard.dashboardController;

// FIX: Class name now matches filename
public class addeditproductController {

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

    // This method populates the form with existing data
    public void setItemToEdit(Inventory_management_bin item) {
        this.itemToEdit = item;
        if (item != null) {
            descriptionField.setText(item.getItem_des());
            volumeField.setText(String.valueOf(item.getVolume()));
            categoryField.setText(item.getCategory());
            salesOfftakeField.setText(String.valueOf(item.getSot()));
            stocksOnHandField.setText(String.valueOf(item.getSoh()));
        }
    }

    private void handleContinue() {
        if (!validateFields()) return;

        try {
            // 1. Get Authentication
            if (dashboardControllerRef != null) {
                this.idToken = dashboardControllerRef.getIdToken();
            }
            if (idToken == null) {
                showAlert("Error", "User not authenticated.");
                return;
            }

            // 2. Prepare Data
            int itemCode = itemToEdit.getItem_code(); // Keep original ID
            String description = descriptionField.getText().trim();
            int volume = Integer.parseInt(volumeField.getText().trim());
            String category = categoryField.getText().trim();
            int salesOfftake = Integer.parseInt(salesOfftakeField.getText().trim());
            int stocksOnHand = Integer.parseInt(stocksOnHandField.getText().trim());

            // 3. Update Firestore (Background Thread)
            new Thread(() -> {
                try {
                    String projectId = FirebaseConfig.getProjectId();
                    String documentPath = "inventory/" + itemCode;
                    
                    // Build JSON payload
                    JSONObject fields = new JSONObject();
                    fields.put("item_code", new JSONObject().put("integerValue", itemCode));
                    fields.put("item_des", new JSONObject().put("stringValue", description));
                    fields.put("volume", new JSONObject().put("integerValue", volume));
                    fields.put("category", new JSONObject().put("stringValue", category));
                    fields.put("sot", new JSONObject().put("integerValue", salesOfftake));
                    fields.put("soh", new JSONObject().put("integerValue", stocksOnHand));
                    
                    JSONObject doc = new JSONObject();
                    doc.put("fields", fields);

                    // Send PATCH request
                    // We use updateMask to ensure we only update specific fields if the doc exists
                    String url = "https://firestore.googleapis.com/v1/projects/" + projectId + "/databases/(default)/documents/" + documentPath + 
                                 "?updateMask.fieldPaths=item_des&updateMask.fieldPaths=volume&updateMask.fieldPaths=category&updateMask.fieldPaths=sot&updateMask.fieldPaths=soh";
                    
                    java.net.URL u = new java.net.URL(url);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                    conn.setRequestMethod("PATCH");
                    conn.setRequestProperty("Authorization", "Bearer " + idToken);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    
                    try (java.io.OutputStream os = conn.getOutputStream()) {
                        os.write(doc.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    }

                    int responseCode = conn.getResponseCode();
                    
                    javafx.application.Platform.runLater(() -> {
                        if (responseCode == 200) {
                            showAlert("Success", "Product updated successfully.");
                            if (dashboardControllerRef != null) {
                                dashboardControllerRef.inventory_management_query();
                                dashboardControllerRef.addInventoryActionNotification("edit", description);
                            }
                            handleCancel(); // Close window
                        } else {
                            showAlert("Error", "Update failed. Code: " + responseCode);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> showAlert("Error", "Failed to update: " + e.getMessage()));
                }
            }).start();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers.");
        }
    }

    private boolean validateFields() {
        return !descriptionField.getText().trim().isEmpty() && 
               !volumeField.getText().trim().isEmpty();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.showAndWait();
    }
}