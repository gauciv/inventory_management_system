package forecasting;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import database.database_utility;
import java.util.Arrays;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ForecastingController {
    @FXML private LineChart<String, Number> forecastChart;
    @FXML private ComboBox<String> forecastProductComboBox;
    @FXML private Label forecastAccuracyLabel;
    @FXML private Label forecastTrendLabel;
    @FXML private Label forecastRecommendationsLabel;
    
    private final ForecastingModel forecastingModel;
    
    public ForecastingController() {
        this.forecastingModel = new ForecastingModel(0.2, 0.1, 0.3); // Default smoothing factors
    }
    
    @FXML
    public void initialize() {
        System.out.println("Initializing ForecastingController...");
        loadProducts();
        forecastProductComboBox.setOnAction(event -> {
            System.out.println("Product selection changed to: " + forecastProductComboBox.getValue());
            updateForecast();
        });
        
        // Set default text
        forecastChart.setTitle("Sales Forecast");
        forecastTrendLabel.setText("Select a product to view trend analysis");
        forecastRecommendationsLabel.setText("Select a product to view recommendations");
        
        System.out.println("ForecastingController initialization complete.");
    }
    
    private void loadProducts() {
        System.out.println("Loading products...");
        try (Connection conn = database_utility.connect()) {
            String query = "SELECT DISTINCT item_description FROM sale_offtake";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
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
            }
        } catch (SQLException e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateForecast() {
        String selectedProduct = forecastProductComboBox.getValue();
        if (selectedProduct == null) return;
        
        try (Connection conn = database_utility.connect()) {
            // Get historical data
            String query = "SELECT jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec " +
                         "FROM sale_offtake WHERE item_description = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, selectedProduct);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double[] historicalData = new double[12];
                for (int i = 0; i < 12; i++) {
                    historicalData[i] = rs.getDouble(i + 1);
                }
                
                // Generate 6-month forecast
                double[] forecast = forecastingModel.forecast(historicalData, 6);
                
                // Update chart
                updateChart(historicalData, forecast);
                
                // Calculate and display accuracy
                double accuracy = ForecastingModel.calculateAccuracy(
                    Arrays.copyOfRange(historicalData, 6, 12),
                    Arrays.copyOfRange(forecast, 0, 6)
                );
                forecastAccuracyLabel.setText(String.format("Forecast Accuracy: %.2f%%", accuracy));
                
                // Update trend analysis
                updateTrendAnalysis(historicalData, forecast);
                
                // Update recommendations
                updateRecommendations(historicalData, forecast);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateChart(double[] historical, double[] forecast) {
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
    }
    
    private void updateTrendAnalysis(double[] historical, double[] forecast) {
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
        double avgHistorical = Arrays.stream(historical).average().orElse(0);
        double avgForecast = Arrays.stream(forecast).average().orElse(0);
        double change = ((avgForecast - avgHistorical) / avgHistorical) * 100;
        
        StringBuilder recommendations = new StringBuilder();
        
        if (change > 15) {
            recommendations.append("Consider increasing inventory levels by ").append(String.format("%.0f", change)).append("%. ");
            recommendations.append("Review supply chain capacity.");
        } else if (change < -15) {
            recommendations.append("Consider reducing inventory levels. ");
            recommendations.append("Review pricing strategy and marketing efforts.");
        } else {
            recommendations.append("Maintain current inventory levels. ");
            recommendations.append("Monitor market conditions.");
        }
        
        forecastRecommendationsLabel.setText(recommendations.toString());
    }
}
