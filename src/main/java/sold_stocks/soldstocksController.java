package sold_stocks;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import firebase.FirestoreClient;
import firebase.FirebaseConfig;
import org.json.JSONObject;

public class soldstocksController {

    @FXML private Pane sold_pane;
    @FXML private TextField volumeField;
    @FXML private TextField categoryField;
    @FXML private TextField salesOfftakeField;
    @FXML private TextField stocksOnHandField;
    @FXML private TextField soldStocksField;
    @FXML private Button continueButton;

    private int itemCode = -1;
    private int currentSoh = 0;
    private dashboard.dashboardController dashboardControllerRef;

    private String idToken;
    
    @FXML
    private void Exit() {
        // Get the stage (window) that contains the button
        Stage stage = (Stage) sold_pane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void initialize() {
        if (continueButton != null) {
            continueButton.setOnAction(e -> handleContinue());
        }
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    private void handleContinue() {
        if (itemCode == -1) {
            showAlert("Error", "No item selected.");
            return;
        }

        String soldStocksStr = soldStocksField.getText();
        int soldStocks;
        try {
            soldStocks = Integer.parseInt(soldStocksStr);
        } catch (NumberFormatException ex) {
            showAlert("Input Error", "Please enter a valid number for stocks sold.");
            return;
        }

        if (soldStocks > currentSoh) {
            showAlert("Error", "Not enough stocks available. Current stock on hand: " + currentSoh);
            return;
        }

        int updatedSoh = currentSoh - soldStocks;
        if (idToken == null && dashboardControllerRef != null) {
            try { this.idToken = dashboardControllerRef.getIdToken(); } catch (Exception ignored) {}
        }
        if (idToken == null) {
            showAlert("Error", "User not authenticated. Please log in again.");
            return;
        }
        System.out.println("Updating sold stocks and sales data in Firestore...");
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
                        showAlert("Success", "Stock sold successfully and sales data updated.");
                        if (dashboardControllerRef != null) {
                            dashboardControllerRef.addSoldStockNotification(soldStocks, volumeField.getText() + "mL");
                            dashboardControllerRef.inventory_management_query();
                        }
                        Stage stage = (Stage) sold_pane.getScene().getWindow();
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

    public void setSelectedItemDescription(String description) {
        // Implement if needed to show item description
    }

    public void setItemData(int itemCode, int volume, String category, int salesOfftake, int stocksOnHand) {
        this.itemCode = itemCode;
        this.currentSoh = stocksOnHand;

        volumeField.setText(String.valueOf(volume));
        categoryField.setText(category);
        salesOfftakeField.setText(String.valueOf(salesOfftake));
        stocksOnHandField.setText(String.valueOf(stocksOnHand));
    }

    public void setDashboardController(dashboard.dashboardController controller) {
        this.dashboardControllerRef = controller;
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

