package forecasting;

import firebase.FirestoreClient;
import firebase.FirebaseConfig;
import javafx.application.Platform;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class ForecastingController {
    
    // --- Firebase Credentials ---
    private String idToken;
    private String projectId;

    public void setIdToken(String idToken) {
        this.idToken = idToken;
        this.projectId = FirebaseConfig.getProjectId();
    }
    // ----------------------------

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
        this.forecastingModel = new ForecastingModel(0.2, 0.1, 0.3); // Smoothing factors
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
            
            System.out.println("Initializing ForecastingController UI...");
            
            // --- UI STYLING (Same as before) ---
            if (forecastChart != null) {
                forecastChart.setStyle("-fx-background-color: transparent;");
                forecastChart.getXAxis().setTickLabelFill(javafx.scene.paint.Color.WHITE);
                forecastChart.getYAxis().setTickLabelFill(javafx.scene.paint.Color.WHITE);
                forecastChart.setTitle("Sales Forecast");
            }
            
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
                
                // REMOVED: loadProducts(); <--- THIS WAS THE CAUSE OF THE ERROR
                // Instead, call loadProducts() manually after setting ID Token.
            }

            // ... (Rest of UI setup for formulaComboBox and HelpButton) ...
            if (forecastFormulaComboBox != null) {
                forecastFormulaComboBox.getItems().addAll("Holt-Winters", "Moving Average", "Simple Average", "Linear Programming");
                forecastFormulaComboBox.setStyle("-fx-background-color: white; -fx-text-fill: #181739; -fx-font-size: 14px; -fx-background-radius: 5;");
                forecastFormulaComboBox.setOnAction(e -> updateForecast());
            }
            if (formulaHelpButton != null) formulaHelpButton.setOnAction(e -> showFormulaHelp());
            if (forecastPlaceholderLabel != null) forecastPlaceholderLabel.setVisible(true);

            // CALL THIS NOW since we are in the flow where token is set
            if (this.idToken != null) {
                loadProducts();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadProducts() {
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
        if (!ready) {
            clearForecastView();
            return;
        }

        if (forecastPlaceholderLabel != null) {
            forecastPlaceholderLabel.setVisible(false);
        }
        
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
                        // 1. Prepare Data
                        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                        double[] historicalData = new double[months.length];
                        
                        for (int m = 0; m < months.length; m++) {
                            String monthKey = months[m].toLowerCase();
                            if (fields.has(monthKey)) {
                                historicalData[m] = fields.getJSONObject(monthKey).getInt("integerValue");
                            } else {
                                historicalData[m] = 0;
                            }
                        }

                        // 2. Clear previous chart data
                        Platform.runLater(() -> {
                            forecastChart.getData().clear();
                            XYChart.Series<String, Number> historySeries = new XYChart.Series<>();
                            historySeries.setName("Historical Sales");
                            for (int m = 0; m < months.length; m++) {
                                historySeries.getData().add(new XYChart.Data<>(months[m], historicalData[m]));
                            }
                            forecastChart.getData().add(historySeries);
                        });

                        // 3. Calculate Forecast
                        List<Double> forecastResults = new ArrayList<>();
                        int monthsToForecast = 3;

                        if ("Holt-Winters".equals(selectedFormula)) {
                            forecastResults = forecastingModel.calculateHoltWinters(historicalData, monthsToForecast);
                        } else if ("Moving Average".equals(selectedFormula)) {
                            forecastResults = forecastingModel.calculateMovingAverage(historicalData, monthsToForecast);
                        } else if ("Simple Average".equals(selectedFormula)) {
                            forecastResults = forecastingModel.calculateSimpleAverage(historicalData, monthsToForecast);
                        } else if ("Linear Programming".equals(selectedFormula)) {
                            forecastResults = forecastingModel.calculateLinearRegression(historicalData, monthsToForecast);
                        }

                        // 4. Update Chart
                        final List<Double> finalForecast = forecastResults;
                        Platform.runLater(() -> {
                            XYChart.Series<String, Number> forecastSeries = new XYChart.Series<>();
                            forecastSeries.setName("Forecast (" + selectedFormula + ")");
                            
                            String[] futureMonths = {"Next Jan", "Next Feb", "Next Mar"};
                            
                            for (int k = 0; k < finalForecast.size() && k < futureMonths.length; k++) {
                                forecastSeries.getData().add(new XYChart.Data<>(futureMonths[k], finalForecast.get(k)));
                            }
                            forecastChart.getData().add(forecastSeries);
                            
                            if (!finalForecast.isEmpty()) {
                                double lastForecast = finalForecast.get(finalForecast.size() - 1);
                                double lastHistory = historicalData[historicalData.length-1];
                                
                                if (forecastTrendLabel != null) {
                                    forecastTrendLabel.setText(lastForecast > lastHistory ? "Trend: Increasing 📈" : "Trend: Decreasing 📉");
                                }
                                if (forecastRecommendationsLabel != null) {
                                    forecastRecommendationsLabel.setText(lastForecast > 100 ? "Recommendation: Increase Stock" : "Recommendation: Monitor Levels");
                                }
                                if (forecastAccuracyLabel != null) {
                                    forecastAccuracyLabel.setText("Formula: " + selectedFormula);
                                }
                            }
                        });
                        break; 
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to fetch historical data: " + e.getMessage());
        }
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

    private void clearForecastView() {
        Platform.runLater(() -> {
            if (forecastChart != null) {
                forecastChart.getData().clear();
            }
            if (forecastAccuracyLabel != null) {
                forecastAccuracyLabel.setText("Select a product and formula.");
            }
            if (forecastTrendLabel != null) {
                forecastTrendLabel.setText("Trend: N/A");
            }
            if (forecastRecommendationsLabel != null) {
                forecastRecommendationsLabel.setText("Recommendations: N/A");
            }
            if (forecastPlaceholderLabel != null) {
                forecastPlaceholderLabel.setVisible(true);
            }
        });
    }

    private void showFormulaHelp() {
        try {
            Stage helpStage = new Stage();
            helpStage.initStyle(StageStyle.TRANSPARENT);
            VBox content = new VBox(15);
            content.getStyleClass().add("help-popup");
            content.setPrefWidth(450);
            
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
            
            HBox titleBox = new HBox();
            titleBox.getStyleClass().add("title-box");
            titleBox.setAlignment(Pos.CENTER_LEFT);
            Label titleIcon = new Label("📊");
            titleIcon.getStyleClass().add("title-icon");
            Label title = new Label("Forecasting Formulas");
            title.getStyleClass().add("title-text");
            titleBox.getChildren().addAll(titleIcon, title);
            
            VBox formulasBox = new VBox();
            formulasBox.getStyleClass().add("formulas-box");
            
            VBox hwBox = createFormulaBox("Holt-Winters Method", "Triple exponential smoothing...", "📈");
            VBox maBox = createFormulaBox("Moving Average", "Averages a fixed number of recent periods...", "📊");
            VBox saBox = createFormulaBox("Simple Average", "Takes the mean of all historical data...", "📉");
            VBox lpBox = createFormulaBox("Linear Programming", "Uses linear regression...", "📊");
            
            formulasBox.getChildren().addAll(hwBox, maBox, saBox, lpBox);
            content.getChildren().addAll(windowControls, titleBox, formulasBox);
            
            final Delta dragDelta = new Delta();
            content.setOnMousePressed(mouseEvent -> {
                dragDelta.x = helpStage.getX() - mouseEvent.getScreenX();
                dragDelta.y = helpStage.getY() - mouseEvent.getScreenY();
            });
            content.setOnMouseDragged(mouseEvent -> {
                helpStage.setX(mouseEvent.getScreenX() + dragDelta.x);
                helpStage.setY(mouseEvent.getScreenY() + dragDelta.y);
            });
            
            Scene scene = new Scene(content);
            scene.setFill(null);
            
            String cssPath = "/styles/forecasting-help.css";
            if (getClass().getResource(cssPath) != null) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }
            
            helpStage.setScene(scene);
            helpStage.show();
            
        } catch (Exception e) {
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
    
    private static class Delta { double x, y; }

    public void refreshProductList() {
        loadProducts();
    }

    
}