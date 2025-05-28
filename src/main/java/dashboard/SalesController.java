package dashboard;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import database.database_utility;

public class SalesController {
    @FXML private LineChart<String, Number> salesChart;
    @FXML private BarChart<String, Number> salesBarChart;
    @FXML private AreaChart<String, Number> salesAreaChart;
    @FXML private Label totalSalesLabel;
    @FXML private Label topProductLabel;
    @FXML private Label salesDateLabel;
    @FXML private Label growthRateLabel;
    @FXML private Label averageSalesLabel;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private ComboBox<String> chartTypeComboBox;
    @FXML private ComboBox<String> compareProductComboBox;
    @FXML private Button exportButton;
    @FXML private Button totalSalesButton;
    @FXML private Button compareButton;

    private List<XYChart.Series<String, Number>> currentData = new ArrayList<>();
    private static final String[] CHART_COLORS = {
        "#4CAF50", "#2196F3", "#FFC107", "#E91E63", "#9C27B0",
        "#00BCD4", "#FF5722", "#795548", "#607D8B", "#3F51B5"
    };

    public void initialize() {
        System.out.println("Initializing SalesController...");
        Platform.runLater(() -> {
            setupControls();
            setupSalesChart();
            setupClock();
            updateTotalSales(); // Show total sales by default
            System.out.println("SalesController initialization complete.");
        });
    }

    private void setupControls() {
        // Setup date pickers with fixed start date and current end date
        LocalDate fixedStartDate = LocalDate.of(2024, 5, 28); // May 28, 2024
        LocalDate currentEndDate = LocalDate.now();
        
        startDate.setValue(fixedStartDate);
        endDate.setValue(currentEndDate);
        
        // Disable date pickers to prevent user modification
        startDate.setDisable(true);
        endDate.setDisable(true);
        
        // Update sales data when dates change (though they won't since they're disabled)
        startDate.setOnAction(e -> updateTotalSales());
        endDate.setOnAction(e -> updateTotalSales());

        // Setup export button
        exportButton.setOnAction(e -> exportData());

        // Setup total sales button
        totalSalesButton.setOnAction(e -> updateTotalSales());

        // Setup compare button
        compareButton.setOnAction(e -> showProductSelectionDialog());
    }

    private void loadProducts() {
        try {
            String query = "SELECT DISTINCT item_description FROM sale_offtake ORDER BY item_description";
            Object[] result = database_utility.query(query);
            if (result != null && result.length == 2) {
                ResultSet rs = (ResultSet) result[1];
                compareProductComboBox.getItems().clear();
                while (rs.next()) {
                    compareProductComboBox.getItems().add(rs.getString("item_description"));
                }
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void updateChartType(String chartType) {
        salesChart.setVisible(false);
        salesBarChart.setVisible(false);
        salesAreaChart.setVisible(false);

        switch (chartType) {
            case "Line Chart":
                salesChart.setVisible(true);
                break;
            case "Bar Chart":
                salesBarChart.setVisible(true);
                break;
            case "Area Chart":
                salesAreaChart.setVisible(true);
                break;
        }
        
        // Update data for the selected chart type
        updateChartData(currentData);
    }

    private void updateChartData(List<XYChart.Series<String, Number>> data) {
        salesChart.getData().clear();
        for (XYChart.Series<String, Number> series : data) {
            salesChart.getData().add(series);
        }
    }

    private void addComparisonSeries(String productName) {
        try {
            String query = "SELECT jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, `dec` " +
                          "FROM sale_offtake WHERE item_description = ?";
            
            Object[] result = database_utility.query(query, productName);
            if (result != null && result.length == 2) {
                ResultSet rs = (ResultSet) result[1];
                if (rs.next()) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName(productName);

                    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    
                    for (String month : months) {
                        series.getData().add(new XYChart.Data<>(month, rs.getInt(month.toLowerCase())));
                    }

                    currentData.add(series);
                    updateChartData(currentData);
                }
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to load comparison data: " + e.getMessage());
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
        // TODO: Implement Excel export using Apache POI
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
        
        // Initialize labels
        if (totalSalesLabel != null) totalSalesLabel.setText("Loading...");
        if (topProductLabel != null) topProductLabel.setText("Loading...");
        if (growthRateLabel != null) growthRateLabel.setText("Growth Rate: Loading...");
        if (averageSalesLabel != null) averageSalesLabel.setText("Avg. Monthly Sales: Loading...");

        // Style axis
        CategoryAxis xAxis = (CategoryAxis) salesChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) salesChart.getYAxis();
        
        xAxis.setLabel("Month");
        yAxis.setLabel("Sales Volume (Units)");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
        yAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
        xAxis.setStyle("-fx-text-fill: white;");
        yAxis.setStyle("-fx-text-fill: white;");
        
        // Additional styling for axis labels
        xAxis.lookup(".axis-label").setStyle("-fx-text-fill: white;");
        yAxis.lookup(".axis-label").setStyle("-fx-text-fill: white;");
        
        // Add some padding to axis ranges for better visualization
        yAxis.setAutoRanging(true);
        yAxis.setMinorTickCount(2);
        
        // Initialize labels with loading state
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
        // Set button states: total sales active, compare inactive
        totalSalesButton.setStyle("-fx-background-color: #0A1196; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-color: #00b4ff; -fx-border-width: 2;");
        compareButton.setStyle("-fx-background-color: #181739; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-color: #AEB9E1; -fx-border-width: 2;");
        
        // Reset top product card to default state
        if (topProductLabel != null) {
            topProductLabel.setText("Loading...");
            topProductLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        }
        
        Connection conn = null;
        try {
            String monthlySalesQuery = "SELECT " +
                "SUM(jan) as Jan, SUM(feb) as Feb, SUM(mar) as Mar, " +
                "SUM(apr) as Apr, SUM(may) as May, SUM(jun) as Jun, " +
                "SUM(jul) as Jul, SUM(aug) as Aug, SUM(sep) as Sep, " +
                "SUM(oct) as Oct, SUM(nov) as Nov, SUM(`dec`) as `Dec` " +
                "FROM sale_offtake";

            Object[] result = database_utility.query(monthlySalesQuery);
            if (result != null && result.length == 2) {
                conn = (Connection) result[0];
                ResultSet rs = (ResultSet) result[1];

                if (rs.next()) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Total Sales Volume");

                    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    int annualTotal = 0;
                    double previousMonth = 0;
                    double totalSales = 0;
                    int monthCount = 0;
                    boolean hasData = false;

                    for (String month : months) {
                        double value = rs.getDouble(month);
                        if (!rs.wasNull()) {
                            hasData = true;
                        }
                        annualTotal += value;
                        totalSales += value;
                        monthCount++;
                        // Calculate growth rate
                        if (previousMonth > 0) {
                            double growthRate = ((value - previousMonth) / previousMonth) * 100;
                            Platform.runLater(() -> 
                                growthRateLabel.setText(String.format("Growth Rate: %.1f%%", growthRate))
                            );
                        }
                        previousMonth = value;
                        series.getData().add(new XYChart.Data<>(month, value));
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
                } else {
                    Platform.runLater(() -> {
                        updateChartData(new ArrayList<>());
                        if (totalSalesLabel != null) totalSalesLabel.setText("No sales data available");
                        if (averageSalesLabel != null) averageSalesLabel.setText("Avg. Monthly Sales: N/A");
                    });
                }
                rs.close();
            } else {
                Platform.runLater(() -> {
                    updateChartData(new ArrayList<>());
                    if (totalSalesLabel != null) totalSalesLabel.setText("Error loading data (no result)");
                    if (averageSalesLabel != null) averageSalesLabel.setText("Avg. Monthly Sales: N/A");
                });
            }

            // Get top product
            String topProductQuery = 
                "SELECT item_description, " +
                "(jan + feb + mar + apr + may + jun + jul + aug + sep + oct + nov + `dec`) as total_sales " +
                "FROM sale_offtake " +
                "ORDER BY total_sales DESC LIMIT 1";

            Object[] topProductResult = database_utility.query(topProductQuery);
            if (topProductResult != null && topProductResult.length == 2) {
                ResultSet rs = (ResultSet) topProductResult[1];
                if (rs.next()) {
                    String topProduct = rs.getString("item_description");
                    int topSales = rs.getInt("total_sales");
                    Platform.runLater(() -> {
                        if (topProductLabel != null) {
                            topProductLabel.setText(String.format("%s\nAnnual Volume: %,d units", topProduct, topSales));
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (topProductLabel != null) topProductLabel.setText("No top product data");
                    });
                }
                rs.close();
            } else {
                Platform.runLater(() -> {
                    if (topProductLabel != null) topProductLabel.setText("Error loading top product");
                });
            }

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
        } finally {
            if (conn != null) {
                database_utility.close(conn);
            }
        }
    }

    public void showProductSelectionDialog() {
        // Use a custom FXML dialog for product selection with checkboxes (max 10)
        showProductSelectionDialogFXML();
    }

    // Placeholder for the new FXML-based dialog
    private void showProductSelectionDialogFXML() {
        try {
            // Set button states: compare active, total inactive
            totalSalesButton.setStyle("-fx-background-color: #181739; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-color: #AEB9E1; -fx-border-width: 2;");
            compareButton.setStyle("-fx-background-color: #0A1196; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-color: #00b4ff; -fx-border-width: 2;");
            
            // Get list of products
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
            // Load FXML dialog
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
                // Set button states: compare active, total inactive
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
        Connection conn = null;
        try {
            currentData.clear(); // Always clear previous comparison data
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                             "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

            // Update top product card for comparison mode
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
                
                // Update labels for comparison mode
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

    // Component injection method
    public void injectComponents(LineChart<String, Number> salesChart, 
                               Label totalSalesLabel, 
                               Label topProductLabel, 
                               Label salesDateLabel,
                               DatePicker startDate,
                               DatePicker endDate,
                               Button exportButton,
                               Label growthRateLabel,
                               Label averageSalesLabel,
                               Button totalSalesButton,
                               Button compareButton) {
        this.salesChart = salesChart;
        this.totalSalesLabel = totalSalesLabel;
        this.topProductLabel = topProductLabel;
        this.salesDateLabel = salesDateLabel;
        this.startDate = startDate;
        this.endDate = endDate;
        this.exportButton = exportButton;
        this.growthRateLabel = growthRateLabel;
        this.averageSalesLabel = averageSalesLabel;
        this.totalSalesButton = totalSalesButton;
        this.compareButton = compareButton;
        
        // After injecting components, set up the controls
        setupControls();
        setupSalesChart();
        setupClock();
        updateTotalSales();
    }
}
