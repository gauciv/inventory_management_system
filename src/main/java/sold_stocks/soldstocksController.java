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

        if (idToken == null && dashboardControllerRef != null) {
            try { this.idToken = dashboardControllerRef.getIdToken(); } catch (Exception ignored) {}
        }
        if (idToken == null) {
            showAlert("Error", "User not authenticated. Please log in again.");
            return;
        }

        final int amountSold = soldStocks;
        final int newSoh = currentSoh - amountSold;
        
        // Determine current month (e.g., "dec")
        String currentMonth = java.time.LocalDate.now().getMonth().toString().substring(0, 3).toLowerCase();

        System.out.println("Processing Sale: " + amountSold + " items. Month: " + currentMonth);

        new Thread(() -> {
            try {
                String projectId = FirebaseConfig.getProjectId();
                String documentPath = "inventory/" + itemCode;
                
                // STEP 1: Fetch current document to get the existing sales count for this month
                String getUrl = "https://firestore.googleapis.com/v1/projects/" + projectId + "/databases/(default)/documents/" + documentPath;
                String jsonResponse = FirestoreClient.getDocument(projectId, "inventory/" + itemCode, idToken);
                
                int currentMonthSales = 0;
                JSONObject docJson = new JSONObject(jsonResponse);
                JSONObject fields = docJson.getJSONObject("fields");
                
                // Check if the month field exists, if so, get its value
                if (fields.has(currentMonth)) {
                    currentMonthSales = fields.getJSONObject(currentMonth).getInt("integerValue");
                }
                
                int newMonthSales = currentMonthSales + amountSold;

                // STEP 2: Prepare the Update (SOH + Monthly Sales)
                JSONObject updateFields = new JSONObject();
                
                // Update SOH
                updateFields.put("soh", new JSONObject().put("integerValue", newSoh));
                
                // Update Monthly Sales (e.g., "dec")
                updateFields.put(currentMonth, new JSONObject().put("integerValue", newMonthSales));

                JSONObject updateDoc = new JSONObject();
                updateDoc.put("fields", updateFields);
                String jsonBody = updateDoc.toString();

                // STEP 3: Send POST (Patch) Request
                String patchUrl = "https://firestore.googleapis.com/v1/projects/" + projectId + "/databases/(default)/documents/" + documentPath 
                                + "?updateMask.fieldPaths=soh&updateMask.fieldPaths=" + currentMonth;
                                
                java.net.URL u = new java.net.URL(patchUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                conn.setRequestProperty("Authorization", "Bearer " + idToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("Success", "Sold " + amountSold + " items. \nUpdated SOH: " + newSoh + "\nUpdated " + currentMonth.toUpperCase() + " Sales: " + newMonthSales);
                        
                        if (dashboardControllerRef != null) {
                            dashboardControllerRef.addSoldStockNotification(amountSold, volumeField.getText() + "mL");
                            // Refresh data to reflect changes in graphs immediately
                            dashboardControllerRef.inventory_management_query();
                            dashboardControllerRef.handleTotalSales(); // Refresh Sales Graph
                        }
                        Stage stage = (Stage) sold_pane.getScene().getWindow();
                        stage.close();
                    });
                } else {
                    java.util.Scanner scanner = new java.util.Scanner(conn.getErrorStream(), "UTF-8").useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    throw new Exception("Update failed: " + response);
                }

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> showAlert("Error", "Transaction failed: " + e.getMessage()));
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