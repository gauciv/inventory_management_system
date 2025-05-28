package forecasting;

import javafx.application.Platform;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import database.database_utility;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class ForecastingController {
    private AreaChart<String, Number> forecastChart;
    private ComboBox<String> forecastProductComboBox;
    private Label forecastAccuracyLabel;
    private Label forecastTrendLabel;
    private Label forecastRecommendationsLabel;
    private ComboBox<String> forecastFormulaComboBox;
    private Label forecastPlaceholderLabel;
    private Button formulaHelpButton;
    
    private final ForecastingModel forecastingModel;
    
    public ForecastingController() {
        this.forecastingModel = new ForecastingModel(0.2, 0.1, 0.3); // Smoothing factors for trend, seasonal, and random components
    }
    
    public void initialize(AreaChart<String, Number> chart, ComboBox<String> productCombo,
                         Label accuracyLabel, Label trendLabel, Label recommendationsLabel, 
                         ComboBox<String> formulaCombo, Label placeholderLabel, Button helpButton) {
        try {
            this.forecastChart = chart;
            this.forecastProductComboBox = productCombo;
            this.forecastAccuracyLabel = accuracyLabel;
            this.forecastTrendLabel = trendLabel;
            this.forecastRecommendationsLabel = recommendationsLabel;
            this.forecastFormulaComboBox = formulaCombo;
            this.forecastPlaceholderLabel = placeholderLabel;
            this.formulaHelpButton = helpButton;
            
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
                forecastProductComboBox.setTooltip(new Tooltip("Select the product to forecast sales for."));
                forecastProductComboBox.setStyle("-fx-background-color: white; -fx-text-fill: #181739; -fx-font-size: 14px; -fx-background-radius: 5;");
                forecastProductComboBox.setPromptText("Choose a product");
                forecastProductComboBox.setOnAction(e -> {
                    boolean hasProduct = forecastProductComboBox.getValue() != null;
                    if (forecastFormulaComboBox != null) {
                        forecastFormulaComboBox.setDisable(!hasProduct);
                    }
                    updateForecast();
                });
                loadProducts();
            }
            
            // Configure formula combo box
            if (forecastFormulaComboBox != null) {
                forecastFormulaComboBox.setTooltip(new Tooltip("Select the forecasting formula to use."));
                forecastFormulaComboBox.getItems().clear();
                forecastFormulaComboBox.getItems().addAll("Holt-Winters", "Moving Average", "Simple Average");
                forecastFormulaComboBox.setValue(null);
                forecastFormulaComboBox.setPromptText("Choose a formula");
                forecastFormulaComboBox.setStyle("-fx-background-color: white; -fx-text-fill: #181739; -fx-font-size: 14px; -fx-background-radius: 5;");
                forecastFormulaComboBox.setDisable(true);
                forecastFormulaComboBox.setOnAction(e -> {
                    String selectedProduct = forecastProductComboBox != null ? forecastProductComboBox.getValue() : null;
                    if (selectedProduct == null) {
                        showWarning("Selection Required", "Please select a product first before choosing a formula.");
                        forecastFormulaComboBox.setValue(null);
                        forecastFormulaComboBox.setDisable(true);
                        return;
                    }
                    updateForecast();
                });
            }

            // Configure help button
            if (formulaHelpButton != null) {
                formulaHelpButton.setOnAction(e -> showFormulaHelp());
            }
            
            // Show placeholder initially
            if (forecastPlaceholderLabel != null) {
                forecastPlaceholderLabel.setVisible(true);
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
                forecastProductComboBox.setValue(null); // Show prompt
            }
            
            String query = "SELECT DISTINCT item_description FROM sale_offtake ORDER BY item_description";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                int count = 0;
                while (rs.next()) {
                    String product = rs.getString("item_description");
                    if (product != null) {
                        forecastProductComboBox.getItems().add(product);
                        count++;
                        System.out.println("Added product: " + product);
                    }
                }
                System.out.println("Loaded " + count + " products");
                
                if (count == 0) {
                    showWarning("No Products", "No products found in the database.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading products: " + e.getMessage());
            showError("Database Error", "Failed to load products: " + e.getMessage());
        }
    }
    
    private void updateForecast() {
        String selectedProduct = forecastProductComboBox != null ? forecastProductComboBox.getValue() : null;
        String selectedFormula = forecastFormulaComboBox != null ? forecastFormulaComboBox.getValue() : null;
        boolean ready = selectedProduct != null && selectedFormula != null;
        if (forecastPlaceholderLabel != null) {
            forecastPlaceholderLabel.setVisible(!ready);
        }
        if (!ready) return;
        
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
                            double[] forecast;
                            switch (selectedFormula) {
                                case "Moving Average" -> forecast = movingAverageForecast(historicalData, 6, 3); // window=3
                                case "Simple Average" -> forecast = simpleAverageForecast(historicalData, 6);
                                default -> forecast = forecastingModel.forecast(historicalData, 6); // Holt-Winters
                            }
                            
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
    
    // --- Additional Forecasting Methods ---
    private double[] movingAverageForecast(double[] data, int periodsAhead, int window) {
        double[] forecast = new double[periodsAhead];
        double sum = 0;
        int n = data.length;
        for (int i = n - window; i < n; i++) sum += data[i];
        double avg = sum / window;
        Arrays.fill(forecast, avg);
        return forecast;
    }
    private double[] simpleAverageForecast(double[] data, int periodsAhead) {
        double avg = Arrays.stream(data).average().orElse(0);
        double[] forecast = new double[periodsAhead];
        Arrays.fill(forecast, avg);
        return forecast;
    }

    private void showFormulaHelp() {
        try {
            // Create a new stage for the popup
            Stage helpStage = new Stage();
            helpStage.initStyle(StageStyle.UNDECORATED);
            
            // Create the content
            VBox content = new VBox(10);
            content.setStyle("-fx-background-color: #081739; -fx-padding: 20; -fx-border-color: #AEB9E1; -fx-border-width: 1;");
            content.setPrefWidth(400);
            
            // Add title
            Label title = new Label("Forecasting Formulas");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
            
            // Add formula descriptions
            VBox formulasBox = new VBox(15);
            
            // Holt-Winters description
            VBox hwBox = new VBox(5);
            Label hwTitle = new Label("Holt-Winters Method");
            hwTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            Label hwDesc = new Label("Triple exponential smoothing that captures level, trend, and seasonality. Best for data with clear seasonal patterns.");
            hwDesc.setStyle("-fx-text-fill: #AEB9E1; -fx-wrap-text: true;");
            hwBox.getChildren().addAll(hwTitle, hwDesc);
            
            // Moving Average description
            VBox maBox = new VBox(5);
            Label maTitle = new Label("Moving Average");
            maTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            Label maDesc = new Label("Averages a fixed number of recent periods to smooth out fluctuations. Good for stable data with minimal seasonality.");
            maDesc.setStyle("-fx-text-fill: #AEB9E1; -fx-wrap-text: true;");
            maBox.getChildren().addAll(maTitle, maDesc);
            
            // Simple Average description
            VBox saBox = new VBox(5);
            Label saTitle = new Label("Simple Average");
            saTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            Label saDesc = new Label("Takes the mean of all historical data points. Best for very stable data with no clear trends or seasonality.");
            saDesc.setStyle("-fx-text-fill: #AEB9E1; -fx-wrap-text: true;");
            saBox.getChildren().addAll(saTitle, saDesc);
            
            formulasBox.getChildren().addAll(hwBox, maBox, saBox);
            
            // Add close button
            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #0A1196; -fx-text-fill: white; -fx-background-radius: 5;");
            closeButton.setOnAction(e -> helpStage.close());
            
            // Add window controls
            HBox windowControls = new HBox(10);
            windowControls.setAlignment(Pos.TOP_RIGHT);
            
            Button minimizeBtn = new Button("-");
            minimizeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            minimizeBtn.setOnAction(e -> helpStage.setIconified(true));
            
            Button closeBtn = new Button("×");
            closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            closeBtn.setOnAction(e -> helpStage.close());
            
            windowControls.getChildren().addAll(minimizeBtn, closeBtn);
            
            // Add all components
            content.getChildren().addAll(windowControls, title, formulasBox, closeButton);
            
            // Make window draggable
            final Delta dragDelta = new Delta();
            content.setOnMousePressed(mouseEvent -> {
                dragDelta.x = helpStage.getX() - mouseEvent.getScreenX();
                dragDelta.y = helpStage.getY() - mouseEvent.getScreenY();
            });
            content.setOnMouseDragged(mouseEvent -> {
                helpStage.setX(mouseEvent.getScreenX() + dragDelta.x);
                helpStage.setY(mouseEvent.getScreenY() + dragDelta.y);
            });
            
            // Show the popup
            Scene scene = new Scene(content);
            scene.setFill(null);
            helpStage.setScene(scene);
            helpStage.show();
            
        } catch (Exception e) {
            System.err.println("Error showing formula help: " + e.getMessage());
            e.printStackTrace();
            showError("Help Error", "Failed to show formula help: " + e.getMessage());
        }
    }
    
    // Helper class for window dragging
    private static class Delta {
        double x, y;
    }
}
