package forecasting;

import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import database.database_utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class ForecastingController {
    private LineChart<String, Number> forecastChart;
    private ComboBox<String> forecastProductComboBox;
    private Label forecastAccuracyLabel;
    private Label forecastTrendLabel;
    private Label forecastRecommendationsLabel;
    
    private final ForecastingModel forecastingModel;
    
    public ForecastingController() {
        this.forecastingModel = new ForecastingModel(0.2, 0.1, 0.3); // Smoothing factors for trend, seasonal, and random components
    }
    
    public void initialize(LineChart<String, Number> chart, ComboBox<String> productCombo,
                         Label accuracyLabel, Label trendLabel, Label recommendationsLabel) {
        try {
            this.forecastChart = chart;
            this.forecastProductComboBox = productCombo;
            this.forecastAccuracyLabel = accuracyLabel;
            this.forecastTrendLabel = trendLabel;
            this.forecastRecommendationsLabel = recommendationsLabel;
            
            System.out.println("Initializing ForecastingController...");
            
            // Configure chart appearance
            if (forecastChart != null) {
                forecastChart.setStyle("-fx-background-color: transparent;");
                forecastChart.getXAxis().setTickLabelFill(javafx.scene.paint.Color.WHITE);
                forecastChart.getYAxis().setTickLabelFill(javafx.scene.paint.Color.WHITE);
                forecastChart.setTitle("Sales Forecast");
            }
            
            // Configure labels
            if (forecastAccuracyLabel != null) {
                forecastAccuracyLabel.setStyle("-fx-text-fill: white;");
                forecastAccuracyLabel.setText("Select a product to view forecast accuracy");
            }
            
            if (forecastTrendLabel != null) {
                forecastTrendLabel.setStyle("-fx-text-fill: white;");
                forecastTrendLabel.setText("Select a product to view trend analysis");
            }
            
            if (forecastRecommendationsLabel != null) {
                forecastRecommendationsLabel.setStyle("-fx-text-fill: white;");
                forecastRecommendationsLabel.setText("Select a product to view recommendations");
            }
            
            // Configure product combo box
            if (forecastProductComboBox != null) {
                forecastProductComboBox.setStyle("-fx-prompt-text-fill: white; -fx-text-fill: white;");
                forecastProductComboBox.setPromptText("Select a product");
                
                // Set up event handler for product selection
                forecastProductComboBox.setOnAction(e -> updateForecast());
                
                // Load initial product data
                loadProducts();
            }
            
            System.out.println("ForecastingController initialization complete.");
            
        } catch (Exception e) {
            System.err.println("Error initializing ForecastingController: " + e.getMessage());
            e.printStackTrace();
            showError("Initialization Error", "Failed to initialize forecasting view: " + e.getMessage());
        }
    }
    
    private void loadProducts() {
        System.out.println("Loading products...");
        try (Connection conn = database_utility.connect()) {
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            // Clear existing items
            if (forecastProductComboBox != null) {
                forecastProductComboBox.getItems().clear();
            }
            
            String query = "SELECT DISTINCT item_description FROM sale_offtake ORDER BY item_description";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                int count = 0;
                while (rs.next()) {
                    String product = rs.getString("item_description");
                    forecastProductComboBox.getItems().add(product);
                    count++;
                    System.out.println("Added product: " + product);
                }
                System.out.println("Loaded " + count + " products");
                
                if (count > 0) {
                    forecastProductComboBox.setValue(forecastProductComboBox.getItems().get(0));
                    updateForecast(); // Load initial forecast
                } else {
                    showWarning("No Products", "No products found in the database.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading products: " + e.getMessage());
            showError("Database Error", "Failed to load products: " + e.getMessage());
        }
    }
    
    private void updateForecast() {
        String selectedProduct = forecastProductComboBox.getValue();
        if (selectedProduct == null) return;
        
        try (Connection conn = database_utility.connect()) {
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }

            // Clear existing chart data
            if (forecastChart != null) {
                Platform.runLater(() -> forecastChart.getData().clear());
            }

            // Get historical data
            String query = "SELECT `jan`, `feb`, `mar`, `apr`, `may`, `jun`, `jul`, `aug`, `sep`, `oct`, `nov`, `dec` " +
                         "FROM sale_offtake WHERE item_description = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, selectedProduct);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double[] historicalData = new double[12];
                        int nonZeroMonths = 0;
                        for (int i = 0; i < 12; i++) {
                            historicalData[i] = rs.getDouble(i + 1);
                            if (historicalData[i] > 0) nonZeroMonths++;
                        }

                        // Require at least 12 months of data
                        if (nonZeroMonths < 12) {
                            showWarning("Insufficient Data", "A full year (12 months) of sales data is required to generate a forecast. Only " + nonZeroMonths + " months available.");
                            if (forecastAccuracyLabel != null) forecastAccuracyLabel.setText("");
                            if (forecastTrendLabel != null) forecastTrendLabel.setText("");
                            if (forecastRecommendationsLabel != null) forecastRecommendationsLabel.setText("");
                            return;
                        }

                        try {
                            // Generate 6-month forecast
                            double[] forecast = forecastingModel.forecast(historicalData, 6);
                            
                            // Update chart and analysis
                            Platform.runLater(() -> {
                                updateChart(historicalData, forecast);
                                updateTrendAnalysis(historicalData, forecast);
                                updateRecommendations(historicalData, forecast);
                                
                                // Calculate and display accuracy
                                try {
                                    double accuracy = calculateAccuracy(
                                        Arrays.copyOfRange(historicalData, 6, 12),
                                        Arrays.copyOfRange(forecast, 0, 6)
                                    );
                                    forecastAccuracyLabel.setText(String.format("Forecast Accuracy: %.1f%%", accuracy));
                                } catch (IllegalArgumentException e) {
                                    forecastAccuracyLabel.setText("Accuracy calculation failed: " + e.getMessage());
                                }
                            });
                            
                        } catch (IllegalArgumentException e) {
                            showWarning("Forecast Error", "Unable to generate forecast: " + e.getMessage());
                        }
                    } else {
                        showWarning("No Data", "No sales data found for " + selectedProduct);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating forecast: " + e.getMessage());
            showError("Database Error", "Failed to update forecast: " + e.getMessage());
        }
    }
    
    private double calculateAccuracy(double[] actual, double[] forecast) {
        if (actual.length != forecast.length || actual.length == 0) {
            throw new IllegalArgumentException("Arrays must be of equal non-zero length");
        }
        
        double sumAbsPercentError = 0.0;
        int validPoints = 0;
        
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] != 0) {  // Avoid division by zero
                double absPercentError = Math.abs((actual[i] - forecast[i]) / actual[i]) * 100;
                sumAbsPercentError += absPercentError;
                validPoints++;
            }
        }
        
        if (validPoints == 0) {
            return 0.0;
        }
        
        // Return accuracy as 100 - MAPE (Mean Absolute Percentage Error)
        return Math.min(100.0, Math.max(0.0, 100.0 - (sumAbsPercentError / validPoints)));
    }
    
    private void updateChart(double[] historical, double[] forecast) {
        if (forecastChart == null) return;
        
        forecastChart.getData().clear();
        
        // Historical data series
        XYChart.Series<String, Number> historicalSeries = new XYChart.Series<>();
        historicalSeries.setName("Historical Sales");
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (int i = 0; i < historical.length; i++) {
            historicalSeries.getData().add(new XYChart.Data<>(months[i], historical[i]));
        }
        
        // Forecast data series
        XYChart.Series<String, Number> forecastSeries = new XYChart.Series<>();
        forecastSeries.setName("Forecast");
        String[] forecastMonths = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        for (int i = 0; i < forecast.length; i++) {
            forecastSeries.getData().add(new XYChart.Data<>(forecastMonths[i], forecast[i]));
        }
        
        forecastChart.getData().add(historicalSeries);
        forecastChart.getData().add(forecastSeries);
        
        // Style the chart series
        historicalSeries.getNode().setStyle("-fx-stroke: #4CAF50;"); // Green for historical
        forecastSeries.getNode().setStyle("-fx-stroke: #2196F3;"); // Blue for forecast
    }
    
    private void updateTrendAnalysis(double[] historical, double[] forecast) {
        if (forecastTrendLabel == null) return;
        
        double avgHistorical = Arrays.stream(historical).average().orElse(0);
        double avgForecast = Arrays.stream(forecast).average().orElse(0);
        double change = ((avgForecast - avgHistorical) / avgHistorical) * 100;
        
        String trend;
        if (change > 5) {
            trend = "Increasing trend detected (+" + String.format("%.1f", change) + "%)";
        } else if (change < -5) {
            trend = "Decreasing trend detected (" + String.format("%.1f", change) + "%)";
        } else {
            trend = "Stable trend (±" + String.format("%.1f", Math.abs(change)) + "%)";
        }
        
        forecastTrendLabel.setText(trend);
    }
    
    private void updateRecommendations(double[] historical, double[] forecast) {
        if (forecastRecommendationsLabel == null) return;
        
        double avgHistorical = Arrays.stream(historical).average().orElse(0);
        double avgForecast = Arrays.stream(forecast).average().orElse(0);
        double change = ((avgForecast - avgHistorical) / avgHistorical) * 100;
        
        StringBuilder recommendations = new StringBuilder();
        
        if (change > 15) {
            recommendations.append("Consider increasing inventory levels by ").append(String.format("%.0f", change)).append("%. ");
            recommendations.append("Review supply chain capacity to meet growing demand.");
        } else if (change < -15) {
            recommendations.append("Consider reducing inventory levels. ");
            recommendations.append("Review pricing strategy and marketing efforts to stimulate demand.");
        } else {
            recommendations.append("Maintain current inventory levels. ");
            recommendations.append("Continue monitoring market conditions for changes.");
        }
        
        forecastRecommendationsLabel.setText(recommendations.toString());
    }
    
    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    private void showWarning(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
