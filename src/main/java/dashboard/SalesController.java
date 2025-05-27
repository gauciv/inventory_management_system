package dashboard;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
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
        if (salesChart == null || totalSalesLabel == null || topProductLabel == null || salesDateLabel == null) {
            System.err.println("Error: Sales components not properly injected");
            return;
        }
        setupSalesChart();
        setupClock();
        Platform.runLater(this::updateSalesData);
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

    private void setupSalesChart() {
        if (salesChart != null) {
            salesChart.setAnimated(false);
            salesChart.setTitle("Sales Volume by Month");
            salesChart.getXAxis().setLabel("Month");
            salesChart.getYAxis().setLabel("Sales Volume (Units)");
            salesChart.setStyle("-fx-background-color: transparent;");
            salesChart.getXAxis().setTickLabelFill(javafx.scene.paint.Color.WHITE);
            salesChart.getYAxis().setTickLabelFill(javafx.scene.paint.Color.WHITE);
            
            // Initialize labels
            if (totalSalesLabel != null) totalSalesLabel.setText("0 units");
            if (topProductLabel != null) topProductLabel.setText("No data available");
            
            // Create an empty series to avoid null pointer
            XYChart.Series<String, Number> emptySeries = new XYChart.Series<>();
            emptySeries.setName("Total Volume");
            salesChart.getData().add(emptySeries);
        }
    }

    private void updateSalesData() {
        Connection conn = null;
        try {
            // First query: Get total monthly sales
            String monthlySalesQuery = "SELECT " +
                "SUM(jan) as Jan, SUM(feb) as Feb, SUM(mar) as Mar, " +
                "SUM(apr) as Apr, SUM(may) as May, SUM(jun) as Jun, " +
                "SUM(jul) as Jul, SUM(aug) as Aug, SUM(sep) as Sep, " +
                "SUM(oct) as Oct, SUM(nov) as Nov, SUM(dec) as Dec " +
                "FROM sale_offtake";

            Object[] result = database.database_utility.query(monthlySalesQuery);
            if (result != null && result.length == 2) {
                conn = (Connection) result[0];
                ResultSet rs = (ResultSet) result[1];

                if (rs.next()) {
                    // Clear existing data
                    Platform.runLater(() -> salesChart.getData().clear());

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Total Volume");

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
                        salesChart.getData().add(series);
                        series.getNode().setStyle("-fx-stroke: #4CAF50;");
                        
                        // Style the data points
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
                                Tooltip.install(node, tooltip);
                            }
                        }
                        
                        if (totalSalesLabel != null) {
                            totalSalesLabel.setText(String.format("%,d units", finalAnnualTotal));
                        }
                    });
                }

                rs.close();
            }

            // Get top product by volume
            String topProductQuery = 
                "SELECT item_description, " +
                "(jan + feb + mar + apr + may + jun + jul + aug + sep + oct + nov + `dec`) as total_sales " +
                "FROM sale_offtake " +
                "ORDER BY total_sales DESC LIMIT 1";

            Object[] topProductResult = database.database_utility.query(topProductQuery);
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
                if (totalSalesLabel != null) totalSalesLabel.setText("0 units");
                if (topProductLabel != null) topProductLabel.setText("No data available");
            });
        } finally {
            if (conn != null) {
                database.database_utility.close(conn);
            }
        }
    }
    
    public void injectComponents(LineChart<String, Number> salesChart, Label totalSalesLabel, 
                                  Label topProductLabel, Label salesDateLabel) {
            this.salesChart = salesChart;
            this.totalSalesLabel = totalSalesLabel;
            this.topProductLabel = topProductLabel;
            this.salesDateLabel = salesDateLabel;
        }
}
