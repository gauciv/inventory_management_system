package dashboard;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import database.database_utility;

public class SalesController {
    @FXML private LineChart<String, Number> salesChart;
    @FXML private Label totalSalesLabel;
    @FXML private Label topProductLabel;
    @FXML private Label salesDateLabel;

    public void initialize() {
        System.out.println("Initializing SalesController...");
        Platform.runLater(() -> {
            setupSalesChart();
            setupClock();
            updateSalesData();
            System.out.println("SalesController initialization complete.");
            
            // Schedule periodic updates for sales data
            javafx.animation.Timeline updateTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.minutes(5), e -> updateSalesData())
            );
            updateTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
            updateTimeline.play();
        });
    }

    private void setupSalesChart() {
        if (salesChart == null) {
            System.err.println("Error: Sales chart not properly injected");
            return;
        }

        // Clear any existing data
        salesChart.getData().clear();
        
        // Configure chart properties
        salesChart.setAnimated(false); // Disable animations for better performance
        salesChart.setCreateSymbols(true);
        salesChart.setTitle("Monthly Sales Overview");
        
        // Style for dark theme
        salesChart.setStyle("-fx-background-color: transparent;");
        salesChart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
        salesChart.lookup(".chart-vertical-grid-lines").setStyle("-fx-stroke: #4c5574;");
        salesChart.lookup(".chart-horizontal-grid-lines").setStyle("-fx-stroke: #4c5574;");
        
        // Style axis
        CategoryAxis xAxis = (CategoryAxis) salesChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) salesChart.getYAxis();
        
        xAxis.setLabel("Month");
        yAxis.setLabel("Sales Volume (Units)");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
        yAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
        xAxis.setStyle("-fx-text-fill: white;");
        yAxis.setStyle("-fx-text-fill: white;");
        
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

    private void updateSalesData() {
        System.out.println("Updating sales data...");
        Connection conn = null;
        try {
            // First query: Get total monthly sales by summing all products
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
                    System.out.println("Processing monthly sales data...");
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Monthly Sales Volume");

                    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    int annualTotal = 0;
                    
                    for (String month : months) {
                        int value = rs.getInt(month);
                        annualTotal += value;
                        series.getData().add(new XYChart.Data<>(month, value));
                    }

                    final int finalAnnualTotal = annualTotal;
                    Platform.runLater(() -> {
                        if (salesChart != null) {
                            salesChart.getData().clear();
                            salesChart.getData().add(series);
                            
                            // Style the series
                            series.getNode().setStyle("-fx-stroke: #4CAF50;"); // Green line
                            
                            // Style the data points and add tooltips
                            for (XYChart.Data<String, Number> data : series.getData()) {
                                Node node = data.getNode();
                                if (node != null) {
                                    node.setStyle("-fx-background-color: #4CAF50, white;" +
                                                "-fx-background-insets: 0, 2;" +
                                                "-fx-background-radius: 5px;" +
                                                "-fx-padding: 5px;");
                                    
                                    Tooltip tooltip = new Tooltip(
                                        String.format("%s: %,d units", data.getXValue(), data.getYValue().intValue())
                                    );
                                    tooltip.setStyle("-fx-font-size: 12px;");
                                    Tooltip.install(node, tooltip);
                                }
                            }
                        }
                        
                        if (totalSalesLabel != null) {
                            totalSalesLabel.setText(String.format("%,d units", finalAnnualTotal));
                        }
                    });
                }

                rs.close();
            }

            // Second query: Get top product by volume
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
                }
                rs.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                if (salesChart != null) salesChart.getData().clear();
                if (totalSalesLabel != null) totalSalesLabel.setText("Error loading data");
                if (topProductLabel != null) topProductLabel.setText("Error loading data");
            });
        } finally {
            if (conn != null) {
                database_utility.close(conn);
            }
        }
    }

    // Component injection methods
    public void injectComponents(LineChart<String, Number> salesChart, Label totalSalesLabel, 
                               Label topProductLabel, Label salesDateLabel) {
        this.salesChart = salesChart;
        this.totalSalesLabel = totalSalesLabel;
        this.topProductLabel = topProductLabel;
        this.salesDateLabel = salesDateLabel;
    }

    public void setSalesChart(LineChart<String, Number> chart) {
        this.salesChart = chart;
    }

    public void setTotalSalesLabel(Label label) {
        this.totalSalesLabel = label;
    }

    public void setTopProductLabel(Label label) {
        this.topProductLabel = label;
    }

    public void setSalesDateLabel(Label label) {
        this.salesDateLabel = label;
    }
}
