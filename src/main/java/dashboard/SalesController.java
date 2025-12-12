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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.database_utility;
import firebase.FirestoreClient;
import firebase.FirebaseConfig;

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
        System.out.println("Initializing SalesController...");
        Platform.runLater(() -> {
            setupControls();
            setupSalesChart();
            setupClock();
            updateTotalSales();
            System.out.println("SalesController initialization complete.");
            
            // Add a delay before showing dashboard
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            delay.setOnFinished(event -> {
                // Show dashboard after delay
                if (mainController != null) {
                    mainController.showDashboard();
                }
            });
            delay.play();
        });
    }

    private void setupControls() {
        // Setup export button
        exportButton.setOnAction(e -> exportData());

        // Setup total sales button
        totalSalesButton.setOnAction(e -> updateTotalSales());

        // Setup compare button
        compareButton.setOnAction(e -> showProductSelectionDialog());
    }

    private void updateChartData(List<XYChart.Series<String, Number>> data) {
        salesChart.getData().clear();
        for (XYChart.Series<String, Number> series : data) {
            salesChart.getData().add(series);
        }
    }

    private void exportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Sales Data");
        
        // Set default filename with current date
        String defaultFileName = "sales_data_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        fileChooser.setInitialFileName(defaultFileName + ".csv");
        
        // Set default directory to user's documents folder
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Documents"));
        
        // Set up file filters with CSV as default
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
        FileChooser.ExtensionFilter excelFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().addAll(csvFilter, excelFilter);
        fileChooser.setSelectedExtensionFilter(csvFilter); // Set CSV as default

        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                // If no extension is provided, default to .csv
                if (!file.getName().toLowerCase().endsWith(".csv") && !file.getName().toLowerCase().endsWith(".xlsx")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }
                
                if (file.getName().toLowerCase().endsWith(".csv")) {
                    exportToCSV(file);
                } else if (file.getName().toLowerCase().endsWith(".xlsx")) {
                    exportToExcel(file);
                }
                showInfo("Success", "Data exported successfully to: " + file.getName());
            } catch (Exception e) {
                e.printStackTrace();
                showError("Export Error", "Failed to export data: " + e.getMessage());
            }
        }
    }

    private void exportToCSV(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write headers
            writer.println("Month,Product,Sales Volume");

            // Write data
            for (XYChart.Series<String, Number> series : currentData) {
                String productName = series.getName();
                for (XYChart.Data<String, Number> data : series.getData()) {
                    writer.printf("%s,%s,%d%n", 
                        data.getXValue(),
                        productName,
                        data.getYValue().intValue()
                    );
                }
            }
        }
    }

    private void exportToExcel(File file) {
        showError("Not Implemented", "Excel export is not yet implemented.");
    }

    private void setupSalesChart() {
        if (salesChart == null) {
            System.err.println("Error: Sales chart not properly injected");
            return;
        }

        // Configure chart properties
        salesChart.setAnimated(false);
        salesChart.setCreateSymbols(true);
        salesChart.setTitle("Monthly Sales Overview");
        salesChart.lookup(".chart-title").setStyle("-fx-text-fill: white;");
        
        // Style for dark theme
        String chartStyle = "-fx-background-color: transparent;";
        salesChart.setStyle(chartStyle);
        
        // Style axis
        CategoryAxis xAxis = (CategoryAxis) salesChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) salesChart.getYAxis();
        
        xAxis.setLabel("Month");
        yAxis.setLabel("Sales Volume (Units)");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
        yAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
        xAxis.setStyle("-fx-text-fill: white;");
        yAxis.setStyle("-fx-text-fill: white;");
        
        xAxis.lookup(".axis-label").setStyle("-fx-text-fill: white;");
        yAxis.lookup(".axis-label").setStyle("-fx-text-fill: white;");
        
        yAxis.setAutoRanging(true);
        yAxis.setMinorTickCount(2);
        
        if (totalSalesLabel != null) {
            totalSalesLabel.setText("Loading...");
            totalSalesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24;");
        }
        if (topProductLabel != null) {
            topProductLabel.setText("Loading...");
            topProductLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        }
    }

    private void setupClock() {
        if (salesDateLabel != null) {
            javafx.animation.Timeline clock = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.ZERO, e -> {
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy | HH:mm:ss");
                    salesDateLabel.setText("DATE: " + now.format(formatter));
                }),
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1))
            );
            clock.setCycleCount(javafx.animation.Animation.INDEFINITE);
            clock.play();
        }
    }

    public void updateTotalSales() {
        System.out.println("Updating total sales data...");
        totalSalesButton.setStyle("-fx-background-color: #0A1196; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-color: #00b4ff; -fx-border-width: 2;");
        compareButton.setStyle("-fx-background-color: #181739; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-color: #AEB9E1; -fx-border-width: 2;");
        
        if (topProductLabel != null) {
            topProductLabel.setText("Loading...");
            topProductLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        }
        
        try {
            if (idToken == null) throw new Exception("No idToken set. User not authenticated.");
            String collectionPath = "inventory?pageSize=1000";
            String response = FirestoreClient.getDocument(projectId, collectionPath, idToken);
            org.json.JSONObject json = new org.json.JSONObject(response);
            org.json.JSONArray docs = json.optJSONArray("documents");
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Sales Volume");
            int annualTotal = 0;
            double previousMonth = 0;
            double totalSales = 0;
            int monthCount = 0;
            boolean hasData = false;
            
            int[] monthTotals = new int[months.length];
            if (docs != null) {
                for (int i = 0; i < docs.length(); i++) {
                    org.json.JSONObject doc = docs.getJSONObject(i);
                    org.json.JSONObject fields = doc.getJSONObject("fields");
                    for (int m = 0; m < months.length; m++) {
                        String month = months[m].toLowerCase();
                        if (fields.has(month)) {
                            int value = fields.getJSONObject(month).getInt("integerValue");
                            monthTotals[m] += value;
                        }
                    }
                }
                for (int m = 0; m < months.length; m++) {
                    int value = monthTotals[m];
                    if (value > 0) hasData = true;
                    annualTotal += value;
                    totalSales += value;
                    monthCount++;
                    
                    if (previousMonth > 0) {
                        double growthRate = ((value - previousMonth) / previousMonth) * 100;
                        final double finalGrowthRate = growthRate;
                        Platform.runLater(() -> growthRateLabel.setText(String.format("Growth Rate: %.1f%%", finalGrowthRate)));
                    }
                    previousMonth = value;
                    series.getData().add(new XYChart.Data<>(months[m], value));
                }
            }
            double averageSales = monthCount > 0 ? totalSales / monthCount : 0;
            final int finalAnnualTotal = annualTotal;
            final double finalAverageSales = averageSales;
            final boolean finalHasData = hasData;
            Platform.runLater(() -> {
                currentData.clear();
                if (finalHasData) {
                    currentData.add(series);
                    updateChartData(currentData);
                    if (totalSalesLabel != null) {
                        totalSalesLabel.setText(String.format("%,d units", finalAnnualTotal));
                    }
                    if (averageSalesLabel != null) {
                        averageSalesLabel.setText(String.format("Avg. Monthly Sales: %,.0f units", finalAverageSales));
                    }
                } else {
                    updateChartData(new ArrayList<>());
                    if (totalSalesLabel != null) totalSalesLabel.setText("No sales data available");
                    if (averageSalesLabel != null) averageSalesLabel.setText("Avg. Monthly Sales: N/A");
                }
                styleChartSeries();
            });
            
            String topProduct = null;
            int topSales = 0;
            if (docs != null) {
                for (int i = 0; i < docs.length(); i++) {
                    org.json.JSONObject doc = docs.getJSONObject(i);
                    org.json.JSONObject fields = doc.getJSONObject("fields");
                    String item_des = fields.getJSONObject("item_des").getString("stringValue");
                    int productTotal = 0;
                    for (String month : months) {
                        if (fields.has(month.toLowerCase())) {
                            productTotal += fields.getJSONObject(month.toLowerCase()).getInt("integerValue");
                        }
                    }
                    if (productTotal > topSales) {
                        topSales = productTotal;
                        topProduct = item_des;
                    }
                }
            }
            final String finalTopProduct = topProduct;
            final int finalTopSales = topSales;
            Platform.runLater(() -> {
                if (finalTopProduct != null && topProductLabel != null) {
                    topProductLabel.setText(String.format("%s\nAnnual Volume: %,d units", finalTopProduct, finalTopSales));
                } else if (topProductLabel != null) {
                    topProductLabel.setText("No top product data");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                updateChartData(new ArrayList<>());
                showError("Data Error", "Failed to load sales data: " + e.getMessage());
                if (totalSalesLabel != null) totalSalesLabel.setText("Error loading data");
                if (topProductLabel != null) topProductLabel.setText("Error loading data");
                if (growthRateLabel != null) growthRateLabel.setText("Growth Rate: N/A");
                if (averageSalesLabel != null) averageSalesLabel.setText("Avg. Monthly Sales: N/A");
            });
        }
    }

    public void showProductSelectionDialog() {
        showProductSelectionDialogFXML();
    }

    private void showProductSelectionDialogFXML() {
        try {
            totalSalesButton.setStyle("-fx-background-color: #181739; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-color: #AEB9E1; -fx-border-width: 2;");
            compareButton.setStyle("-fx-background-color: #0A1196; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-color: #00b4ff; -fx-border-width: 2;");
            
            // Using existing database_utility for products (Legacy compat, ideally move to Firebase)
            String query = "SELECT DISTINCT item_description FROM sale_offtake ORDER BY item_description";
            Object[] result = database_utility.query(query);
            if (result == null || result.length != 2) {
                showError("Error", "Failed to load products");
                return;
            }
            ResultSet rs = (ResultSet) result[1];
            List<String> products = new ArrayList<>();
            while (rs.next()) {
                products.add(rs.getString("item_description"));
            }
            rs.close();
            if (products.isEmpty()) {
                showError("Error", "No products found");
                return;
            }
            
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/dashboard/product_compare_dialog.fxml"));
            javafx.scene.Parent root = loader.load();
            ProductCompareDialogController controller = loader.getController();
            controller.setProducts(products);
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.initOwner(salesChart.getScene().getWindow());
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setTitle("Compare Products");
            controller.setDialogStage(dialogStage);
            controller.setOnConfirm(selectedProducts -> {
                updateComparisonChart(selectedProducts);
                totalSalesButton.setStyle("-fx-background-color: #181739; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-color: #AEB9E1; -fx-border-width: 2;");
                compareButton.setStyle("-fx-background-color: #0A1196; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-color: #00b4ff; -fx-border-width: 2;");
            });
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Dialog Error", "Failed to open product selection dialog: " + e.getMessage());
        }
    }

    private void updateComparisonChart(List<String> products) {
        try {
            currentData.clear(); 
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                             "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

            if (topProductLabel != null) {
                StringBuilder comparisonInfo = new StringBuilder("Comparing Products:\n");
                for (String product : products) {
                    comparisonInfo.append("• ").append(product).append("\n");
                }
                topProductLabel.setText(comparisonInfo.toString());
                topProductLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
            }

            for (int i = 0; i < products.size(); i++) {
                String product = products.get(i);
                String query = "SELECT jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, `dec` " +
                             "FROM sale_offtake WHERE item_description = ?";
                
                Object[] result = database_utility.query(query, product);
                if (result != null && result.length == 2) {
                    ResultSet rs = (ResultSet) result[1];
                    if (rs.next()) {
                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        series.setName(product);

                        for (String month : months) {
                            series.getData().add(new XYChart.Data<>(month, rs.getInt(month.toLowerCase())));
                        }

                        currentData.add(series);
                    }
                    rs.close();
                }
            }

            Platform.runLater(() -> {
                updateChartData(currentData);
                styleChartSeries();
                
                if (totalSalesLabel != null) totalSalesLabel.setText("Comparison Mode");
                if (averageSalesLabel != null) averageSalesLabel.setText("Select products to compare");
                if (growthRateLabel != null) growthRateLabel.setText("Growth Rate: N/A");
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to load comparison data: " + e.getMessage());
        }
    }

    private void styleChartSeries() {
        for (int i = 0; i < currentData.size(); i++) {
            XYChart.Series<String, Number> series = currentData.get(i);
            String color = CHART_COLORS[i % CHART_COLORS.length];
            
            series.getNode().setStyle("-fx-stroke: " + color + ";");
            
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle(
                        "-fx-background-color: " + color + ", white;" +
                        "-fx-background-insets: 0, 2;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-padding: 5px;"
                    );
                    
                    Tooltip tooltip = new Tooltip(
                        String.format("%s - %s: %,d units", 
                            series.getName(),
                            data.getXValue(), 
                            data.getYValue().intValue()
                        )
                    );
                    tooltip.setStyle("-fx-font-size: 12px;");
                    Tooltip.install(node, tooltip);
                }
            }
        }
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void showInfo(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public void injectComponents(AreaChart<String, Number> salesChart,
                               Label totalSalesLabel,
                               Label topProductLabel,
                               Label salesDateLabel,
                               Button exportButton,
                               Label growthRateLabel,
                               Label averageSalesLabel,
                               Button totalSalesButton,
                               Button compareButton) {
        this.salesChart = salesChart;
        this.totalSalesLabel = totalSalesLabel;
        this.topProductLabel = topProductLabel;
        this.salesDateLabel = salesDateLabel;
        this.exportButton = exportButton;
        this.growthRateLabel = growthRateLabel;
        this.averageSalesLabel = averageSalesLabel;
        this.totalSalesButton = totalSalesButton;
        this.compareButton = compareButton;
        
        setupControls();
        setupSalesChart();
        setupClock();
        updateTotalSales();
    }
}