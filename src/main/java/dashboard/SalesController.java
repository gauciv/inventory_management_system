package dashboard;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import firebase.FirestoreClient;
import firebase.FirebaseConfig;
import org.json.JSONObject;
import org.json.JSONArray;

public class SalesController {
    
    // --- Firebase Credentials ---
    private String idToken;
    private String projectId;

    public void setIdToken(String idToken) {
        this.idToken = idToken;
        this.projectId = FirebaseConfig.getProjectId();
    }
    // ----------------------------

    @FXML private AreaChart<String, Number> salesChart;
    @FXML private Label totalSalesLabel;
    @FXML private Label topProductLabel;
    @FXML private Label salesDateLabel;
    @FXML private Label growthRateLabel;
    @FXML private Label averageSalesLabel;
    @FXML private Button exportButton;
    @FXML private Button totalSalesButton;
    @FXML private Button compareButton;

    private List<XYChart.Series<String, Number>> currentData = new ArrayList<>();
    private static final String[] CHART_COLORS = {
        "#4CAF50", "#2196F3", "#FFC107", "#E91E63", "#9C27B0",
        "#00BCD4", "#FF5722", "#795548", "#607D8B", "#3F51B5"
    };

    private dashboardController mainController;

    public void setMainController(dashboardController controller) {
        this.mainController = controller;
    }

    public void initialize() {
        // Run UI setup immediately
        setupControls();
        setupSalesChart();
        setupClock();
        
        // REMOVED: The automatic thread start. 
        // Data loading is now triggered by injectComponents or explicit call.
    }

    private void setupControls() {
        if(exportButton != null) exportButton.setOnAction(e -> exportData());
        if(totalSalesButton != null) totalSalesButton.setOnAction(e -> new Thread(this::updateTotalSales).start());
        if(compareButton != null) compareButton.setOnAction(e -> showProductSelectionDialog());
    }

    private void setupSalesChart() {
        if (salesChart == null) return;
        salesChart.setAnimated(false);
        salesChart.setTitle("Monthly Sales Overview");
        salesChart.lookup(".chart-title").setStyle("-fx-text-fill: white;");
        
        salesChart.setStyle("-fx-background-color: transparent;");
        
        CategoryAxis xAxis = (CategoryAxis) salesChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) salesChart.getYAxis();
        
        xAxis.setLabel("Month");
        yAxis.setLabel("Sales Volume");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
        yAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
    }

    private void setupClock() {
        if (salesDateLabel != null) {
            javafx.animation.Timeline clock = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.ZERO, e -> {
                    salesDateLabel.setText("DATE: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy | HH:mm:ss")));
                }),
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1))
            );
            clock.setCycleCount(javafx.animation.Animation.INDEFINITE);
            clock.play();
        }
    }

    // --- MISSING METHOD RESTORED HERE ---
    private void exportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Sales Data");
        String defaultFileName = "sales_data_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        fileChooser.setInitialFileName(defaultFileName + ".csv");
        
        File userHome = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(userHome);
        
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(csvFilter);

        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }
                exportToCSV(file);
                showInfo("Success", "Data exported successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Export Error", "Failed to export data: " + e.getMessage());
            }
        }
    }

    private void exportToCSV(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Month,Series,Sales Volume");
            for (XYChart.Series<String, Number> series : currentData) {
                String seriesName = series.getName();
                for (XYChart.Data<String, Number> data : series.getData()) {
                    writer.printf("%s,%s,%d%n", data.getXValue(), seriesName, data.getYValue().intValue());
                }
            }
        }
    }

    public void updateTotalSales() {
        Platform.runLater(() -> {
            if(totalSalesButton != null) totalSalesButton.setStyle("-fx-background-color: #0A1196; -fx-text-fill: white; -fx-background-radius: 5;");
            if(compareButton != null) compareButton.setStyle("-fx-background-color: #181739; -fx-text-fill: white; -fx-background-radius: 5;");
            if(totalSalesLabel != null) totalSalesLabel.setText("Loading...");
        });
        
        try {
            if (idToken == null) throw new Exception("No idToken set.");
            
            String collectionPath = "inventory?pageSize=1000";
            String response = FirestoreClient.getDocument(projectId, collectionPath, idToken);
            
            JSONObject json = new JSONObject(response);
            JSONArray docs = json.optJSONArray("documents");
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Sales Volume");
            
            int[] monthTotals = new int[12];
            int annualTotal = 0;
            String topProduct = "-";
            int maxProductSales = 0;

            if (docs != null) {
                for (int i = 0; i < docs.length(); i++) {
                    JSONObject doc = docs.getJSONObject(i);
                    JSONObject fields = doc.getJSONObject("fields");
                    
                    int productTotal = 0;
                    for (int m = 0; m < 12; m++) {
                        String monthKey = months[m].toLowerCase();
                        if (fields.has(monthKey)) {
                            int val = fields.getJSONObject(monthKey).getInt("integerValue");
                            monthTotals[m] += val;
                            productTotal += val;
                        }
                    }
                    if (productTotal > maxProductSales) {
                        maxProductSales = productTotal;
                        if(fields.has("item_des"))
                            topProduct = fields.getJSONObject("item_des").getString("stringValue");
                    }
                }
            }

            for (int m = 0; m < 12; m++) {
                series.getData().add(new XYChart.Data<>(months[m], monthTotals[m]));
                annualTotal += monthTotals[m];
            }
            
            double average = annualTotal / 12.0;
            double growth = 0;
            if (monthTotals[10] > 0) {
                growth = ((double)(monthTotals[11] - monthTotals[10]) / monthTotals[10]) * 100;
            }

            final int finalAnnual = annualTotal;
            final double finalAvg = average;
            final String finalTop = topProduct;
            final int finalTopSales = maxProductSales;
            final double finalGrowth = growth;

            Platform.runLater(() -> {
                salesChart.getData().clear();
                salesChart.getData().add(series);
                currentData.clear();
                currentData.add(series);
                
                if(totalSalesLabel != null) totalSalesLabel.setText(String.format("%,d units", finalAnnual));
                if(averageSalesLabel != null) averageSalesLabel.setText(String.format("Avg: %,.0f", finalAvg));
                if(topProductLabel != null) topProductLabel.setText(finalTop + "\n(" + finalTopSales + ")");
                if(growthRateLabel != null) growthRateLabel.setText(String.format("Growth: %.1f%%", finalGrowth));
                
                styleChartSeries();
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showError("Data Error", "Failed to load sales: " + e.getMessage()));
        }
    }

    public void showProductSelectionDialog() {
        new Thread(() -> {
            try {
                if (idToken == null) return;
                String response = FirestoreClient.getDocument(projectId, "inventory?pageSize=1000", idToken);
                JSONObject json = new JSONObject(response);
                JSONArray docs = json.optJSONArray("documents");
                
                List<String> products = new ArrayList<>();
                if (docs != null) {
                    for (int i = 0; i < docs.length(); i++) {
                        JSONObject fields = docs.getJSONObject(i).getJSONObject("fields");
                        if(fields.has("item_des"))
                             products.add(fields.getJSONObject("item_des").getString("stringValue"));
                    }
                }
                
                Platform.runLater(() -> {
                    System.out.println("Products loaded for compare: " + products.size());
                    showInfo("Coming Soon", "Compare dialog is not yet implemented.");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void styleChartSeries() {
        if (currentData == null) return;
        for (int i = 0; i < currentData.size(); i++) {
            XYChart.Series<String, Number> series = currentData.get(i);
            String color = CHART_COLORS[i % CHART_COLORS.length];
            if(series.getNode() != null) series.getNode().setStyle("-fx-stroke: " + color + ";");
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public void injectComponents(AreaChart<String, Number> salesChart,
                               Label totalSalesLabel, Label topProductLabel, Label salesDateLabel,
                               Button exportButton, Label growthRateLabel, Label averageSalesLabel,
                               Button totalSalesButton, Button compareButton) {
        this.salesChart = salesChart;
        this.totalSalesLabel = totalSalesLabel;
        this.topProductLabel = topProductLabel;
        this.salesDateLabel = salesDateLabel;
        this.exportButton = exportButton;
        this.growthRateLabel = growthRateLabel;
        this.averageSalesLabel = averageSalesLabel;
        this.totalSalesButton = totalSalesButton;
        this.compareButton = compareButton;
        
        initialize(); // Setup UI
        
        // TRIGGER DATA LOAD NOW (Background Thread)
        new Thread(() -> {
            try {
                Thread.sleep(500);
                updateTotalSales();
                Platform.runLater(() -> {
                    if (mainController != null) mainController.showDashboard();
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}