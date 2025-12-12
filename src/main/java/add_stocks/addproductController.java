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

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    private void addNewProduct(String description, int volume, String category, int salesOfftake, int stocksOnHand) {
        if (idToken == null && dashboardControllerRef != null) {
            try { this.idToken = dashboardControllerRef.getIdToken(); } catch (Exception ignored) {}
        }
        if (idToken == null) {
            showAlert("Error", "User not authenticated. Please log in again.");
            return;
        }
        System.out.println("Adding new product to Firestore...");
        new Thread(() -> {
            try {
                String projectId = FirebaseConfig.getProjectId();
                String collectionPath = "inventory";
                JSONObject fields = new JSONObject();
                fields.put("item_code", new JSONObject().put("integerValue", getNextItemCode()));
                fields.put("item_des", new JSONObject().put("stringValue", description));
                fields.put("volume", new JSONObject().put("integerValue", volume));
                fields.put("category", new JSONObject().put("stringValue", category));
                fields.put("sot", new JSONObject().put("integerValue", salesOfftake));
                fields.put("soh", new JSONObject().put("integerValue", stocksOnHand));
                JSONObject doc = new JSONObject();
                doc.put("fields", fields);
                String jsonBody = doc.toString();
                String documentPath = collectionPath; // POST to collection to create new doc
                String url = "https://firestore.googleapis.com/v1/projects/" + projectId + "/databases/(default)/documents/" + documentPath;
                java.net.URL u = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + idToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("Success", "Product added successfully");
                        if (dashboardControllerRef != null) {
                            dashboardControllerRef.inventory_management_query();
                            dashboardControllerRef.addInventoryActionNotification("add", description);
                        }
                        handleCancel();
                    });
                } else {
                    java.util.Scanner scanner = new java.util.Scanner(conn.getErrorStream(), "UTF-8").useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    scanner.close();
                    throw new Exception("Firestore add failed: " + response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> showAlert("Error", "Failed to add product: " + e.getMessage()));
            }
        }).start();
    }

    private void updateExistingProduct(String description, int volume, String category, int salesOfftake, int stocksOnHand) {
        if (idToken == null && dashboardControllerRef != null) {
            try { this.idToken = dashboardControllerRef.getIdToken(); } catch (Exception ignored) {}
        }
        if (idToken == null) {
            showAlert("Error", "User not authenticated. Please log in again.");
            return;
        }
        System.out.println("Updating product in Firestore...");
        new Thread(() -> {
            try {
                String projectId = FirebaseConfig.getProjectId();
                String collectionPath = "inventory";
                String documentId = String.valueOf(itemToEdit.getItem_code());
                String documentPath = collectionPath + "/" + documentId;
                JSONObject fields = new JSONObject();
                fields.put("item_code", new JSONObject().put("integerValue", itemToEdit.getItem_code()));
                fields.put("item_des", new JSONObject().put("stringValue", description));
                fields.put("volume", new JSONObject().put("integerValue", volume));
                fields.put("category", new JSONObject().put("stringValue", category));
                fields.put("sot", new JSONObject().put("integerValue", salesOfftake));
                fields.put("soh", new JSONObject().put("integerValue", stocksOnHand));
                JSONObject doc = new JSONObject();
                doc.put("fields", fields);
                String jsonBody = doc.toString();
                String url = "https://firestore.googleapis.com/v1/projects/" + projectId + "/databases/(default)/documents/" + documentPath + "?updateMask.fieldPaths=item_code&updateMask.fieldPaths=item_des&updateMask.fieldPaths=volume&updateMask.fieldPaths=category&updateMask.fieldPaths=sot&updateMask.fieldPaths=soh";
                java.net.URL u = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Authorization", "Bearer " + idToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("Success", "Product updated successfully");
                        if (dashboardControllerRef != null) {
                            dashboardControllerRef.inventory_management_query();
                        }
                        handleCancel();
                    });
                } else {
                    java.util.Scanner scanner = new java.util.Scanner(conn.getErrorStream(), "UTF-8").useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    scanner.close();
                    throw new Exception("Firestore update failed: " + response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> showAlert("Error", "Failed to update product: " + e.getMessage()));
            }
        }).start();
    }
}