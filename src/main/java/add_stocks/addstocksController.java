package add_stocks;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import database.database_utility;
import firebase.FirestoreClient;
import firebase.FirebaseConfig;
import org.json.JSONObject;
import java.sql.Connection;

public class addstocksController {
    @FXML
    private Pane addPane;
    @FXML @SuppressWarnings("all")
    public TextField text_field1;
    @FXML
    public TextField textfield2;
    @FXML @SuppressWarnings("all")
    public TextField text_field3;
    @FXML @SuppressWarnings("all")
    public TextField text_field4;
    @FXML
    private Text selectedItem;
    @FXML
    public TextField newstock;
    @FXML
    public Button continueButton;
    @FXML
    private Text monthLabel;

    private int itemCode = -1;
    private int currentSoh = 0;
    private dashboard.dashboardController dashboardControllerRef;

    private String idToken;
    
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    // --- Added Missing Setters ---
    public void setDashboardController(dashboard.dashboardController controller) {
        this.dashboardControllerRef = controller;
    }

    public void setItemCodeAndSoh(Integer itemCode, Integer soh) {
        this.itemCode = itemCode != null ? itemCode : -1;
        this.currentSoh = soh != null ? soh : 0;
    }

    public void setSelectedItemDescription(String description) {
        if (selectedItem != null) {
            selectedItem.setText(description);
        }
    }
    // ----------------------------

    @FXML
    private void handleExit() {
        Stage stage = (Stage) addPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleContinue() {
        String newStockStr = newstock.getText();
        int addStock;
        try {
            addStock = Integer.parseInt(newStockStr);
        } catch (NumberFormatException ex) {
            showAlert("Input Error", "Please enter a valid number for new stocks.");
            return;
        }
        int updatedSoh = currentSoh + addStock;
        if (idToken == null && dashboardControllerRef != null) {
            try { this.idToken = dashboardControllerRef.getIdToken(); } catch (Exception ignored) {}
        }
        if (idToken == null) {
            showAlert("Error", "User not authenticated. Please log in again.");
            return;
        }
        System.out.println("Updating stocks in Firestore...");
        new Thread(() -> {
            try {
                String projectId = FirebaseConfig.getProjectId();
                String collectionPath = "inventory";
                String documentId = String.valueOf(itemCode);
                String documentPath = collectionPath + "/" + documentId;
                JSONObject fields = new JSONObject();
                fields.put("soh", new JSONObject().put("integerValue", updatedSoh));
                JSONObject doc = new JSONObject();
                doc.put("fields", fields);
                String jsonBody = doc.toString();
                // Note: The URL here seems manually constructed in your original code, which is fine, 
                // but typically we'd use FirestoreClient.setDocument/PATCH if possible. 
                // Keeping your logic intact:
                String url = "https://firestore.googleapis.com/v1/projects/" + projectId + "/databases/(default)/documents/" + documentPath + "?updateMask.fieldPaths=soh";
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
                        showAlert("Success", "Stocks updated successfully.");
                        if (dashboardControllerRef != null) {
                            dashboardControllerRef.inventory_management_query();
                            String description = selectedItem != null ? selectedItem.getText() : "";
                            dashboardControllerRef.addRecentStockNotification(addStock, description);
                        }
                        Stage stage = (Stage) continueButton.getScene().getWindow();
                        stage.close();
                    });
                } else {
                    java.util.Scanner scanner = new java.util.Scanner(conn.getErrorStream(), "UTF-8").useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    scanner.close();
                    throw new Exception("Firestore update failed: " + response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> showAlert("Error", "Failed to update stocks: " + e.getMessage()));
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}