import firebase.FirestoreClient;
import firebase.FirebaseConfig;
    private String idToken;
    private String projectId;

    public void setIdToken(String idToken) {
        this.idToken = idToken;
        this.projectId = FirebaseConfig.getProjectId();
    }
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
// TODO: Replace all database/database_utility and SQL logic with Firebase SDK
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Insets;


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
                forecastFormulaComboBox.getItems().addAll("Holt-Winters", "Moving Average", "Simple Average", "Linear Programming");
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
        // Load product list from Firestore
        try {
            if (idToken == null) throw new Exception("No idToken set. User not authenticated.");
            String collectionPath = "inventory?pageSize=1000";
            String response = FirestoreClient.getDocument(projectId, collectionPath, idToken);
            org.json.JSONObject json = new org.json.JSONObject(response);
            org.json.JSONArray docs = json.optJSONArray("documents");
            if (forecastProductComboBox != null) {
                forecastProductComboBox.getItems().clear();
                if (docs != null) {
                    for (int i = 0; i < docs.length(); i++) {
                        org.json.JSONObject doc = docs.getJSONObject(i);
                        org.json.JSONObject fields = doc.getJSONObject("fields");
                        String item_des = fields.getJSONObject("item_des").getString("stringValue");
                        forecastProductComboBox.getItems().add(item_des);
                    }
                }
                forecastProductComboBox.setValue(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to load products: " + e.getMessage());
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
        // Fetch historical data for selectedProduct from Firestore
        try {
            if (idToken == null) throw new Exception("No idToken set. User not authenticated.");
            String collectionPath = "inventory?pageSize=1000";
            String response = FirestoreClient.getDocument(projectId, collectionPath, idToken);
            org.json.JSONObject json = new org.json.JSONObject(response);
            org.json.JSONArray docs = json.optJSONArray("documents");
            if (docs != null) {
                for (int i = 0; i < docs.length(); i++) {
                    org.json.JSONObject doc = docs.getJSONObject(i);
                    org.json.JSONObject fields = doc.getJSONObject("fields");
                    String item_des = fields.getJSONObject("item_des").getString("stringValue");
                    if (item_des.equals(selectedProduct)) {
                        // Extract monthly sales data
                        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                        double[] sales = new double[months.length];
                        for (int m = 0; m < months.length; m++) {
                            String month = months[m].toLowerCase();
                            if (fields.has(month)) {
                                sales[m] = fields.getJSONObject(month).getInt("integerValue");
                            } else {
                                sales[m] = 0;
                            }
                        }
                        // Call updateChart, updateTrendAnalysis, updateRecommendations as needed
                        // Example: updateChart(sales, months);
                        // TODO: Implement updateChart, updateTrendAnalysis, updateRecommendations
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to fetch historical data: " + e.getMessage());
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
    private double[] linearProgrammingForecast(double[] data, int periodsAhead) {
        // Linear Programming implementation using simple linear regression
        double[] forecast = new double[periodsAhead];
        int n = data.length;
        
        // Calculate slope and intercept using least squares method
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += data[i];
            sumXY += i * data[i];
            sumX2 += i * i;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        
        // Generate forecast using the linear equation: y = mx + b
        for (int i = 0; i < periodsAhead; i++) {
            forecast[i] = Math.max(0, slope * (n + i) + intercept); // Ensure non-negative values
        }
        
        return forecast;
    }

    private void showFormulaHelp() {
        try {
            // Create a new stage for the popup
            Stage helpStage = new Stage();
            helpStage.initStyle(StageStyle.TRANSPARENT);
            
            // Create the content
            VBox content = new VBox(15);
            content.getStyleClass().add("help-popup");
            content.setPrefWidth(450);
            
            // Window controls
            HBox windowControls = new HBox();
            windowControls.getStyleClass().add("window-controls");
            windowControls.setAlignment(Pos.TOP_RIGHT);
            
            Button minimizeBtn = new Button("─");
            minimizeBtn.getStyleClass().addAll("control-button", "minimize-button");
            minimizeBtn.setOnAction(e -> helpStage.setIconified(true));
            
            Button closeBtn = new Button("×");
            closeBtn.getStyleClass().addAll("control-button", "close-button");
            closeBtn.setOnAction(e -> helpStage.close());
            
            windowControls.getChildren().addAll(minimizeBtn, closeBtn);
            
            // Add title with icon
            HBox titleBox = new HBox();
            titleBox.getStyleClass().add("title-box");
            titleBox.setAlignment(Pos.CENTER_LEFT);
            
            Label titleIcon = new Label("📊");
            titleIcon.getStyleClass().add("title-icon");
            
            Label title = new Label("Forecasting Formulas");
            title.getStyleClass().add("title-text");
            
            titleBox.getChildren().addAll(titleIcon, title);
            
            // Add formula descriptions with improved styling
            VBox formulasBox = new VBox();
            formulasBox.getStyleClass().add("formulas-box");
            
            // Holt-Winters description
            VBox hwBox = createFormulaBox(
                "Holt-Winters Method",
                "Triple exponential smoothing that captures level, trend, and seasonality. Best for data with clear seasonal patterns.",
                "📈"
            );
            
            // Moving Average description
            VBox maBox = createFormulaBox(
                "Moving Average",
                "Averages a fixed number of recent periods to smooth out fluctuations. Good for stable data with minimal seasonality.",
                "📊"
            );
            
            // Simple Average description
            VBox saBox = createFormulaBox(
                "Simple Average",
                "Takes the mean of all historical data points. Best for very stable data with no clear trends or seasonality.",
                "📉"
            );
            
            // Linear Programming description
            VBox lpBox = createFormulaBox(
                "Linear Programming",
                "Uses linear regression to find the best-fit line through historical data points and extrapolate future values. Best for data with clear linear trends.",
                "📊"
            );
            
            formulasBox.getChildren().addAll(hwBox, maBox, saBox, lpBox);
            
            // Add all components
            content.getChildren().addAll(windowControls, titleBox, formulasBox);
            
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
            
            // Load CSS
            String cssPath = "/styles/forecasting-help.css";
            if (getClass().getResource(cssPath) == null) {
                throw new IllegalStateException("Cannot find CSS file: " + cssPath);
            }
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            
            helpStage.setScene(scene);
            helpStage.show();
            
        } catch (Exception e) {
            System.err.println("Error showing formula help: " + e.getMessage());
            e.printStackTrace();
            showError("Help Error", "Failed to show formula help: " + e.getMessage());
        }
    }
    
    private VBox createFormulaBox(String title, String description, String icon) {
        VBox box = new VBox(10);
        box.getStyleClass().add("formula-card");
        
        HBox titleBox = new HBox(10);
        titleBox.getStyleClass().add("formula-title-box");
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("formula-icon");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("formula-title");
        
        titleBox.getChildren().addAll(iconLabel, titleLabel);
        
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("formula-description");
        descLabel.setWrapText(true);
        
        box.getChildren().addAll(titleBox, descLabel);
        return box;
    }
    
    // Helper class for window dragging
    private static class Delta {
        double x, y;
    }

    public void refreshProductList() {
        loadProducts();
    }
}
