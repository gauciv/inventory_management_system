package dashboard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import database.database_utility;
import firebase.FirestoreClient;
import firebase.FirebaseConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import javafx.scene.control.Tooltip;
import forecasting.ForecastingController;
import forecasting.ForecastingModel;
import confirmation.confirmationController;
import sold_stocks.soldStock;
import add_edit_product.addeditproductController;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class dashboardController {
    @FXML private Button minimizeButton;
    @FXML private Button resizeButton;
    @FXML private Button exitButton;
    @FXML private BorderPane borderpane;
    @FXML private TabPane tabpane;
    @FXML private Tab dashboardTab;
    @FXML private Button dashboardbutton;
    @FXML private AnchorPane dashboardpane;
    @FXML private Button inventorybutton;
    @FXML private AnchorPane inventorypane;
    @FXML private Button forecastingbutton;
    @FXML private AnchorPane forecastingpane;
    @FXML private Button salesbutton;
    @FXML private AnchorPane salespane;
    @FXML private Button helpbutton;
    @FXML private AnchorPane helppane;
    @FXML private Button activeButton;
    @FXML private TextField searchField;
    @FXML private AnchorPane addFormContainer;
    @FXML private AnchorPane confirmationContainer;
    @FXML private VBox right_pane;
    @FXML public ComboBox<String> monthComboBox;
    @FXML private AreaChart<String, Number> forecastChart;
    @FXML private ComboBox<String> forecastProductComboBox;
    @FXML private Label forecastAccuracyLabel;
    @FXML private Label forecastTrendLabel;
    @FXML private Label forecastRecommendationsLabel;
    @FXML private Label dateLabel;
    @FXML private Label dateTimeLabel;
    @FXML private AreaChart<String, Number> salesChart;
    @FXML private Label totalSalesLabel;
    @FXML private Label topProductLabel;
    @FXML private Label salesDateLabel;
    @FXML private Label salesTimeLabel;
    @FXML private VBox recent;
    @FXML private ComboBox<String> forecastFormulaComboBox;
    @FXML private Label forecastPlaceholderLabel;
    @FXML private Button formulaHelpButton;
    @FXML private Button exportButton;
    @FXML private Label growthRateLabel;
    @FXML private Label averageSalesLabel;
    @FXML private Button totalSalesButton;
    @FXML private Button compareButton;
    @FXML private Button forecastRefreshButton;

    @FXML private ScrollPane notifScrollPane;
    @FXML private VBox recent1;

    @FXML private Region dashboardIndicator;
    @FXML private Region inventoryIndicator;
    @FXML private Region salesIndicator;
    @FXML private Region forecastingIndicator;
    @FXML private Region helpIndicator;
    
    private Region currentIndicator;

    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isFullscreen = false;
    private double prevWidth = 900;
    private double prevHeight = 450;
    private double prevX, prevY;

    private ForecastingController forecastingController;
    private Timeline clockTimeline;
    private SalesController salesController;

    private javafx.application.HostServices hostServices;

    public void setHostServices(javafx.application.HostServices hostServices) {
        this.hostServices = hostServices;
    }

    private String idToken;
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    private String idToken;
    
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    // --- ADD THIS METHOD ---
    public String getIdToken() {
        return this.idToken;
    }
    // -----------------------

    @FXML
    public void initialize() {
        try {
            // Hide tab headers immediately
            if (tabpane != null) {
                tabpane.lookupAll(".tab-header-area").forEach(node -> {
                    node.setVisible(false);
                    node.setManaged(false);
                });
            }

            // Initially hide all panes
            if (dashboardpane != null) dashboardpane.setVisible(false);
            if (inventorypane != null) inventorypane.setVisible(false);
            if (salespane != null) salespane.setVisible(false);
            if (forecastingpane != null) forecastingpane.setVisible(false);
            if (helppane != null) helppane.setVisible(false);

            // Initialize collections first
            inventory_management_table = FXCollections.observableArrayList();
            
            // Store controller reference in BorderPane's userData
            if (borderpane != null) {
                borderpane.setUserData(this);
            }

            // Get current month name
            String currentMonth = java.time.LocalDate.now().getMonth().toString();
            // Capitalize first letter only
            currentMonth = currentMonth.substring(0, 1).toUpperCase() + currentMonth.substring(1).toLowerCase();

            // Initialize UI components first
            setupTableView();
            setupWindowControls();
            setupFormContainers();
            
            // Set current month as default for monthComboBox
            if (monthComboBox != null) {
                monthComboBox.setStyle("-fx-prompt-text-fill: white; -fx-text-fill: white;");
                monthComboBox.setPromptText("Select a Month");
                monthComboBox.setValue(currentMonth);
                // Auto-refresh inventory table when month changes
                monthComboBox.setOnAction(event -> {
                    inventory_management_query();
                    updateStockNotifications();
                });
            }
            
            // Set current month as default for the dashboard month ComboBox
            ComboBox<String> dashboardMonthCombo = (ComboBox<String>) borderpane.lookup("#month");
            if (dashboardMonthCombo != null) {
                dashboardMonthCombo.setValue(currentMonth);
                dashboardMonthCombo.setOnAction(event -> updateStockNotifications());
            }
            
            // Set default value for stocks ComboBox
            ComboBox<String> stocksCombo = (ComboBox<String>) borderpane.lookup("#stocks");
            if (stocksCombo != null) {
                stocksCombo.setValue("1000");
                stocksCombo.setOnAction(event -> updateStockNotifications());
            }

            // Start clock immediately
            startClock();
            
            // Setup navigation
            setupNavigation();
            
            // Initialize search if available
            if (searchField != null) {
                setupSearch();
            }
            
            // Setup forecast refresh button
            if (forecastRefreshButton != null) {
                forecastRefreshButton.setOnAction(e -> {
                    if (forecastingController != null) {
                        forecastingController.refreshProductList();
                    }
                });
            }

            // Load data in background
            new Thread(() -> {
                try {
                        Platform.runLater(() -> {
                            try {
                                inventory_management_query();
                                updateStockNotifications();
                                initializeForecastingSection();
                                initializeSalesSection();
                                loadNotificationsFromDatabase();
                            } catch (Exception e) {
                                e.printStackTrace();
                                showErrorAlert("Data Loading Error", "Failed to load initial data: " + e.getMessage());
                            }
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> 
                        showErrorAlert("Initialization Error", "Failed to initialize dashboard: " + e.getMessage())
                    );
                }
            }).start();
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Initialization Error", "Failed to initialize the dashboard: " + e.getMessage());
        }
    }

    private void initializeForecastingSection() {
        try {
            // Initialize forecasting controller
            forecastingController = new ForecastingController();
            
            // Configure chart before passing to controller
            if (forecastChart != null) {
                forecastChart.setAnimated(false);
                forecastChart.getXAxis().setLabel("Month");
                forecastChart.getYAxis().setLabel("Sales Volume");
                
                // Style the chart
                forecastChart.setCreateSymbols(true); // Enable data points
                forecastChart.setLegendVisible(true);
                
                // Style the legend
                Node legend = forecastChart.lookup(".chart-legend");
                if (legend != null) {
                    legend.setStyle("-fx-background-color: transparent;");
                    legend.lookupAll(".chart-legend-item")
                          .forEach(item -> item.setStyle("-fx-text-fill: white !important;"));
                }
                
                forecastChart.getStyleClass().add("chart");
                
                NumberAxis yAxis = (NumberAxis) forecastChart.getYAxis();
                yAxis.setAutoRanging(true);
                yAxis.setForceZeroInRange(false);
                yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
                    @Override
                    public String toString(Number object) {
                        String label = String.format("%,.0f", object.doubleValue());
                        if (object.doubleValue() >= 1000) {
                            label = String.format("%,.0fk", object.doubleValue() / 1000);
                        }
                        return label;
                    }
                });

                forecastChart.getData().addListener((ListChangeListener<XYChart.Series<String, Number>>) c -> {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            for (XYChart.Series<String, Number> series : c.getAddedSubList()) {
                                for (XYChart.Data<String, Number> data : series.getData()) {
                                    if (data.getNode() != null) {
                                        Node node = data.getNode();
                                        Tooltip tooltip = new Tooltip();
                                        tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: white; -fx-text-fill: black;");
                                        
                                        String formattedValue;
                                        double value = data.getYValue().doubleValue();
                                        if (value >= 1000) {
                                            formattedValue = String.format("%,.1fk", value / 1000);
                                        } else {
                                            formattedValue = String.format("%,.0f", value);
                                        }
                                        
                                        tooltip.setText(series.getName() + "\nMonth: " + data.getXValue() + "\nSales: " + formattedValue);
                                        Tooltip.install(node, tooltip);
                                    }
                                }
                            }
                        }
                    }
                });
            }
            
            forecastingController.initialize(
                forecastChart,
                forecastProductComboBox,
                forecastAccuracyLabel,
                forecastTrendLabel,
                forecastRecommendationsLabel,
                forecastFormulaComboBox,
                forecastPlaceholderLabel,
                formulaHelpButton
            );
        } catch (Exception e) {
            System.err.println("Error initializing forecasting section: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Initialization Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to initialize forecasting section: " + e.getMessage());
            alert.initStyle(StageStyle.UNDECORATED);
            alert.showAndWait();
        }
    }
  
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.showAndWait();
    }

    private void setupTableView() {
        if (inventory_table == null) {
            showErrorAlert("Initialization Error", "Table view not found in FXML");
            return;
        }

        inventory_table.setItems(inventory_management_table);

        col_number.getStyleClass().add("col-number");
        col_select.getStyleClass().add("col-select");
        col_item_code.getStyleClass().add("col-item-code");
        col_item_des.getStyleClass().add("col-item-des");
        col_volume.getStyleClass().add("col-volume");
        col_category.getStyleClass().add("col-category");
        col_soh.getStyleClass().add("col-soh");
        col_sot.getStyleClass().add("col-sot");

        col_number.setText("#");
        col_select.setText("☐");
        col_item_code.setText("Item Code");
        col_item_des.setText("Product\nDescription");
        col_volume.setText("Volume");
        col_category.setText("Category");
        col_soh.setText("Stocks on\nHand");
        col_sot.setText("Sales\nOfftake");

        inventory_table.getColumns().forEach(column -> {
            column.setStyle("-fx-alignment: CENTER;");
        });

        col_select.setStyle("-fx-alignment: CENTER; -fx-font-size: 16px;");

        col_number.setPrefWidth(50);
        col_select.setPrefWidth(50);
        col_item_code.setPrefWidth(100);
        col_item_des.setPrefWidth(300);
        col_volume.setPrefWidth(100);
        col_category.setPrefWidth(150);
        col_soh.setPrefWidth(100);
        col_sot.setPrefWidth(100);

        col_number.setMinWidth(col_number.getPrefWidth());
        col_select.setMinWidth(col_select.getPrefWidth());
        col_item_code.setMinWidth(col_item_code.getPrefWidth());
        col_item_des.setMinWidth(col_item_des.getPrefWidth());
        col_volume.setMinWidth(col_volume.getPrefWidth());
        col_category.setMinWidth(col_category.getPrefWidth());
        col_soh.setMinWidth(col_soh.getPrefWidth());
        col_sot.setMinWidth(col_sot.getPrefWidth());

        inventory_table.getColumns().forEach(column -> {
            column.setResizable(false);
            column.setReorderable(false);
            column.setSortable(false);
        });

        inventory_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        inventory_table.prefWidthProperty().bind(
            inventorypane.widthProperty().multiply(0.98)
        );
        
        inventory_table.prefHeightProperty().bind(
            inventorypane.heightProperty().multiply(0.85)
        );

        col_number.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(
                () -> inventory_table.getItems().indexOf(cellData.getValue()) + 1
            )
        );
        col_item_code.setCellValueFactory(new PropertyValueFactory<>("item_code"));
        col_item_des.setCellValueFactory(new PropertyValueFactory<>("item_des"));
        col_volume.setCellValueFactory(new PropertyValueFactory<>("volume"));
        col_category.setCellValueFactory(new PropertyValueFactory<>("category"));
        col_soh.setCellValueFactory(new PropertyValueFactory<>("soh"));
        col_sot.setCellValueFactory(new PropertyValueFactory<>("sot"));
        
        col_select.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        
        col_select.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction((ActionEvent _event) -> {
                    Inventory_management_bin bin = getTableRow() != null ? getTableRow().getItem() : null;
                    if (bin != null) {
                        bin.setSelected(checkBox.isSelected());
                    }
                });
            }
            
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Inventory_management_bin bin = getTableRow().getItem();
                    if (bin != null) {
                        checkBox.setSelected(bin.getSelected());
                    }
                    setGraphic(checkBox);
                }
            }
        });

        inventory_table.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
    }
    
    private void setupWindowControls() {
        borderpane.setOnMousePressed((MouseEvent event) -> {
            borderpane.setPickOnBounds(true);
            Stage stage = (Stage) borderpane.getScene().getWindow();
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
            prevX = stage.getX();
            prevY = stage.getY();
        });

        borderpane.setOnMouseDragged((MouseEvent event) -> {
            if (!isFullscreen) {
                Stage stage = (Stage) borderpane.getScene().getWindow();
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        TabSwitch(dashboardbutton, dashboardpane);
        TabSwitch(inventorybutton, inventorypane);
        TabSwitch(forecastingbutton, forecastingpane);
        TabSwitch(salesbutton, salespane);
        TabSwitch(helpbutton, helppane);
    }
    
    private void setupFormContainers() {
        searchField.prefWidthProperty().bind(inventorypane.widthProperty().divide(2).subtract(20));
        inventory_table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        inventorypane.widthProperty().addListener(o -> centerAddFormContainer());
        inventorypane.heightProperty().addListener(o -> centerAddFormContainer());
        inventorypane.widthProperty().addListener(o -> centerConfirmationContainer());
        inventorypane.heightProperty().addListener(o -> centerConfirmationContainer());

        Platform.runLater(() -> centerAddFormContainer());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/confirmation/confirmation_form.fxml"));
            Parent confirmationForm = loader.load();
            confirmationContainer.getChildren().setAll(confirmationForm);
            confirmationContainer.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        monthComboBox.getItems().addAll(
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
        );

        monthComboBox.setValue("January");

        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            String baseStyle = "-fx-background-color: #081739; -fx-background-radius: 30; " +
                    "-fx-background-insets: 0; -fx-border-radius: 30; -fx-border-color: transparent; " +
                    "-fx-prompt-text-fill: rgba(170,170,170,0.5);";

            if (newVal) {
                searchField.setStyle(baseStyle + " -fx-text-fill: white;");
            } else {
                searchField.setStyle(baseStyle + " -fx-text-fill: black;");
            }
        });
    }

    private void centerAddFormContainer() {
        if (!addFormContainer.isVisible()) return;

        double parentWidth = inventorypane.getWidth();
        double parentHeight = inventorypane.getHeight();

        double formWidth = addFormContainer.getWidth() <= 0 ? addFormContainer.getPrefWidth() : addFormContainer.getWidth();
        double formHeight = addFormContainer.getHeight() <= 0 ? addFormContainer.getPrefHeight() : addFormContainer.getHeight();

        double leftAnchor = (parentWidth - formWidth) / 2;
        double topAnchor = (parentHeight - formHeight) / 2;

        AnchorPane.clearConstraints(addFormContainer);
        AnchorPane.setLeftAnchor(addFormContainer, leftAnchor);
        AnchorPane.setTopAnchor(addFormContainer, topAnchor);
        AnchorPane.setRightAnchor(addFormContainer, null);
        AnchorPane.setBottomAnchor(addFormContainer, null);
    }

    private void centerConfirmationContainer() {
        if (!confirmationContainer.isVisible()) return;

        double parentWidth = inventorypane.getWidth();
        double parentHeight = inventorypane.getHeight();

        confirmationContainer.applyCss();
        confirmationContainer.layout();

        double formWidth = confirmationContainer.getWidth();
        double formHeight = confirmationContainer.getHeight();

        if (formWidth <= 0) formWidth = confirmationContainer.getPrefWidth();
        if (formHeight <= 0) formHeight = confirmationContainer.getPrefHeight();

        double leftAnchor = (parentWidth - formWidth) / 2;
        double topAnchor = (parentHeight - formHeight) / 2;

        AnchorPane.clearConstraints(confirmationContainer);
        AnchorPane.setLeftAnchor(confirmationContainer, leftAnchor);
        AnchorPane.setTopAnchor(confirmationContainer, topAnchor);
        AnchorPane.setRightAnchor(confirmationContainer, null);
        AnchorPane.setBottomAnchor(confirmationContainer, null);
    }

    private void styleActiveButton(Button selectedButton) {
        List<Button> validButtons = List.of(
                dashboardbutton, inventorybutton, salesbutton,
                forecastingbutton, helpbutton
        );

        if (!validButtons.contains(selectedButton)) {
            return;
        }

        for (Button btn : validButtons) {
            btn.getStyleClass().remove("active");
            btn.setStyle("-fx-background-color: transparent;");
            
            HBox parent = (HBox) btn.getParent();
            if (parent != null) {
                parent.getStyleClass().remove("active");
                parent.getChildren().stream()
                    .filter(node -> node instanceof Region && node.getStyleClass().contains("nav-indicator"))
                    .findFirst()
                    .ifPresent(indicator -> indicator.getStyleClass().remove("active"));
            }
        }

        HBox parent = (HBox) selectedButton.getParent();
        if (parent != null) {
            parent.getStyleClass().add("active");
            parent.getChildren().stream()
                .filter(node -> node instanceof Region && node.getStyleClass().contains("nav-indicator"))
                .findFirst()
                .ifPresent(indicator -> indicator.getStyleClass().add("active"));
        }
        
        selectedButton.getStyleClass().add("active");
        selectedButton.setStyle("-fx-background-color: #2D3C7233;");
    }

    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleResize() {
        Stage stage = (Stage) resizeButton.getScene().getWindow();
        if (!isFullscreen) {
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();
            prevX = stage.getX();
            prevY = stage.getY();

            stage.setX(0);
            stage.setY(0);
            stage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
            stage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
            stage.getScene().getRoot().requestLayout();

            isFullscreen = true;
        } else {
            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevWidth);
            stage.setHeight(prevHeight);
            stage.getScene().getRoot().requestLayout();

            isFullscreen = false;
        }
        Platform.runLater(this::centerAddFormContainer);
    }

    @FXML
    private void handleExit() {
        System.out.println("Exit clicked");
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    public void hideTabHeaders() {
        Platform.runLater(() -> {
            tabpane.lookupAll(".tab-header-area").forEach(node -> {
                node.setVisible(false);
                node.setManaged(false);
            });
        });
    }

    private void setupNavigation() {
        dashboardbutton.setOnAction(e -> TabSwitch(dashboardbutton, dashboardpane));
        inventorybutton.setOnAction(e -> TabSwitch(inventorybutton, inventorypane));
        salesbutton.setOnAction(e -> TabSwitch(salesbutton, salespane));
        forecastingbutton.setOnAction(e -> TabSwitch(forecastingbutton, forecastingpane));
        helpbutton.setOnAction(e -> TabSwitch(helpbutton, helppane));
        
        TabSwitch(dashboardbutton, dashboardpane);
    }

    private void TabSwitch(Button button, AnchorPane pane) {
        Region indicator = null;
        if (button == dashboardbutton) indicator = dashboardIndicator;
        else if (button == inventorybutton) indicator = inventoryIndicator;
        else if (button == salesbutton) indicator = salesIndicator;
        else if (button == forecastingbutton) indicator = forecastingIndicator;
        else if (button == helpbutton) indicator = helpIndicator;

        if (indicator != null) {
            handleNavigation(button, indicator, pane);
        }
    }

    private void handleNavigation(Button button, Region indicator, Node content) {
        if (currentIndicator != null) {
            currentIndicator.getStyleClass().remove("active");
            currentIndicator.getParent().getStyleClass().remove("active");
        }
        
        dashboardbutton.getStyleClass().remove("active");
        inventorybutton.getStyleClass().remove("active");
        salesbutton.getStyleClass().remove("active");
        forecastingbutton.getStyleClass().remove("active");
        helpbutton.getStyleClass().remove("active");
        
        button.getStyleClass().add("active");
        indicator.getStyleClass().add("active");
        indicator.getParent().getStyleClass().add("active");
        
        currentIndicator = indicator;
        
        if (content != null) {
            dashboardpane.setVisible(false);
            inventorypane.setVisible(false);
            salespane.setVisible(false);
            forecastingpane.setVisible(false);
            helppane.setVisible(false);
            
            content.setVisible(true);
        }
        
        String tabText = button.getText().trim();
        for (Tab tab : tabpane.getTabs()) {
            if (tab.getText().equalsIgnoreCase(tabText) || 
                tab.getText().equalsIgnoreCase(tabText.replace(" ", ""))) {
                tabpane.getSelectionModel().select(tab);
                break;
            }
        }
    }

    @FXML
    private void handleAddButton() {
        try {
            int checkedCount = 0;
            Inventory_management_bin selectedItem = null;
            
            for (Inventory_management_bin item : inventory_table.getItems()) {
                if (item.getSelected()) {
                    checkedCount++;
                    selectedItem = item;
                }
            }
            
            String fxmlPath;
            String title;
            
            if (checkedCount > 1) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select only one item.");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                return;
            } else if (checkedCount == 1) {
                fxmlPath = "/addStocks/addstocks_form.fxml";
                title = "Add Stocks Form";

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent addForm = loader.load();
                add_stocks.addstocksController controller = loader.getController();
                if (selectedItem != null && controller != null) {
                    controller.text_field1.setText(String.valueOf(selectedItem.getVolume()));
                    controller.textfield2.setText(selectedItem.getCategory());
                    controller.text_field3.setText(String.valueOf(selectedItem.getSot()));
                    controller.text_field4.setText(String.valueOf(selectedItem.getSoh()));
                    controller.setSelectedItemDescription(selectedItem.getFormattedItemDesc());
                    controller.setItemCodeAndSoh(selectedItem.getItem_code(), selectedItem.getSoh());
                    controller.setDashboardController(this);
                }
                Scene scene = new Scene(addForm);
                scene.setFill(null);
                Stage stage = new Stage();
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.setTitle(title);
                stage.setScene(scene);
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/intervein_logo_no_text.png")));
                Bounds paneBounds = right_pane.localToScreen(right_pane.getBoundsInLocal());
                stage.show();
                double centerX = paneBounds.getMinX() + (paneBounds.getWidth() / 2) - (stage.getWidth() / 2);
                double centerY = paneBounds.getMinY() + (paneBounds.getHeight() / 2) - (stage.getHeight() / 2);
                stage.setX(centerX);
                stage.setY(centerY);
                stage.toFront();
                return;
            } else {
                fxmlPath = "/addStocks/addproduct.fxml";
                title = "Add Product Form";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent addForm = loader.load();
            
            add_stocks.addproductController controller = loader.getController();
            if (controller == null) {
                throw new RuntimeException("Failed to get controller for add product form");
            }
            controller.setDashboardController(this);
            
            Scene scene = new Scene(addForm);
            scene.setFill(null);
            
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/intervein_logo_no_text.png")));
            
            Bounds paneBounds = right_pane.localToScreen(right_pane.getBoundsInLocal());
            stage.show();
            
            double centerX = paneBounds.getMinX() + (paneBounds.getWidth() / 2) - (stage.getWidth() / 2);
            double centerY = paneBounds.getMinY() + (paneBounds.getHeight() / 2) - (stage.getHeight() / 2);
            
            stage.setX(centerX);
            stage.setY(centerY);
            stage.toFront();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load form: " + e.getMessage());
            alert.initStyle(StageStyle.UNDECORATED);
            alert.showAndWait();
        } catch (RuntimeException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.initStyle(StageStyle.UNDECORATED);
            alert.showAndWait();
        }
    }

    @FXML
    private void handleSoldButton() {
        try {
            int checkedCount = 0;
            Inventory_management_bin selectedItem = null;
            
            for (Inventory_management_bin item : inventory_table.getItems()) {
                if (item.getSelected()) {
                    checkedCount++;
                    selectedItem = item;
                }
            }
            
            if (checkedCount == 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select an item to mark as sold.");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                return;
            } else if (checkedCount > 1) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select only one item.");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                return;
            }
            
            soldStock dialog = new soldStock();
            Stage owner = (Stage) right_pane.getScene().getWindow();
            dialog.showPopup(
                owner,
                inventorypane,
                selectedItem.getItem_code(),
                selectedItem.getFormattedItemDesc(),
                selectedItem.getVolume(),
                selectedItem.getCategory(),
                selectedItem.getSot(),
                selectedItem.getSoh()
            );

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to open sold stocks form: " + e.getMessage());
            alert.initStyle(StageStyle.UNDECORATED);
            alert.showAndWait();
        }
    }

    @FXML
    private void handleConfirmationButton() {
        try {
            int checkedCount = 0;
            Inventory_management_bin selectedItem = null;
            
            for (Inventory_management_bin item : inventory_table.getItems()) {
                if (item.getSelected()) {
                    checkedCount++;
                    selectedItem = item;
                }
            }
            
            if (checkedCount == 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select an item to delete.");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                return;
            }

            final Inventory_management_bin itemToDelete = selectedItem;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/confirmation/confirmation_form.fxml"));
            Parent confirmationForm = loader.load();
            confirmationController controller = loader.getController();
            
            controller.setDeletionCallback(new confirmationController.DeletionCallback() {
                @Override
                public void onConfirmDeletion() {
                    inventory_table.getItems().remove(itemToDelete);
                    deleteInventoryItem(itemToDelete);
                }

                @Override
                public void onCancelDeletion() {
                }
            });

            confirmationContainer.getChildren().setAll(confirmationForm);
            confirmationForm.setLayoutX(0);
            confirmationForm.setTranslateX(0);
            
            confirmationContainer.setVisible(true);
            confirmationContainer.toFront();

            Platform.runLater(() -> {
                confirmationContainer.applyCss();
                confirmationContainer.layout();
                centerConfirmationContainer();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleContinueClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/confirmation/confirmation_form.fxml"));
            Parent confirmationForm = loader.load();

            confirmationContainer.getChildren().setAll(confirmationForm);
            confirmationForm.setLayoutX(0);
            confirmationForm.setTranslateX(0);

            confirmationContainer.setVisible(true);
            confirmationContainer.toFront();

            Platform.runLater(() -> {
                confirmationContainer.applyCss();
                confirmationContainer.layout();
                centerConfirmationContainer();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private TableView<Inventory_management_bin> inventory_table;
    @FXML
    private TableColumn<Inventory_management_bin, Integer> col_number;
    @FXML
    private TableColumn<Inventory_management_bin, Integer> col_item_code;
    @FXML
    private TableColumn<Inventory_management_bin, String> col_item_des;
    @FXML
    private TableColumn<Inventory_management_bin, Integer> col_volume;
    @FXML
    private TableColumn<Inventory_management_bin, String> col_category;
    @FXML
    private TableColumn<Inventory_management_bin, Integer> col_soh;
    @FXML
    private TableColumn<Inventory_management_bin, Integer> col_sot;
    @FXML 
    private TableColumn<Inventory_management_bin, Boolean> col_select;
    private ObservableList<Inventory_management_bin> inventory_management_table;

    public String getSelectedMonthColumn() {
        if (monthComboBox != null && monthComboBox.getValue() != null) {
            String month = monthComboBox.getValue().toLowerCase().substring(0, 3);
            return month;
        }
        return "dec";
    }

    public void inventory_management_query() {
        if (inventory_management_table != null) {
            inventory_management_table.clear();
        }
        System.out.println("Fetching inventory data from Firestore...");
        new Thread(() -> {
            try {
                if (idToken == null) {
                    throw new Exception("No idToken set. User not authenticated.");
                }
                String projectId = FirebaseConfig.getProjectId();
                String collectionPath = "inventory";
                String urlPath = collectionPath + "?pageSize=1000";
                String response = FirestoreClient.getDocument(projectId, urlPath, idToken);
                org.json.JSONObject json = new org.json.JSONObject(response);
                org.json.JSONArray docs = json.optJSONArray("documents");
                if (docs != null) {
                    for (int i = 0; i < docs.length(); i++) {
                        org.json.JSONObject doc = docs.getJSONObject(i);
                        org.json.JSONObject fields = doc.getJSONObject("fields");
                        int item_code = fields.getJSONObject("item_code").getInt("integerValue");
                        String item_des = fields.getJSONObject("item_des").getString("stringValue");
                        int volume = fields.getJSONObject("volume").getInt("integerValue");
                        String category = fields.getJSONObject("category").getString("stringValue");
                        int sot = fields.getJSONObject("sot").getInt("integerValue");
                        int soh = fields.getJSONObject("soh").getInt("integerValue");
                        Inventory_management_bin bin = new Inventory_management_bin(item_code, item_des, volume, category, sot, soh);
                        javafx.application.Platform.runLater(() -> inventory_management_table.add(bin));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Firestore Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to fetch inventory data: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void deleteInventoryItem(Inventory_management_bin itemToDelete) {
        new Thread(() -> {
            try {
                if (idToken == null) {
                    throw new Exception("No idToken set. User not authenticated.");
                }
                String projectId = FirebaseConfig.getProjectId();
                String collectionPath = "inventory";
                String documentId = String.valueOf(itemToDelete.getItem_code());
                FirestoreClient.deleteDocument(projectId, collectionPath, documentId, idToken);
                Platform.runLater(this::inventory_management_query);
                addInventoryActionNotification("delete", itemToDelete.getItem_des());
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to delete item from Firestore: " + e.getMessage());
                    alert.initStyle(StageStyle.UNDECORATED);
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedDate = currentTime.format(dateFormatter);
            String formattedTime = currentTime.format(timeFormatter);
            
            if (dateLabel != null) {
                dateLabel.setText("DATE: " + formattedDate + " | " + formattedTime);
            }
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    @FXML
    private void handleRefreshData() {
        if (inventory_management_table != null) {
            inventory_management_table.clear();
        }
        inventory_management_query();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Refresh Complete");
        alert.setHeaderText(null);
        alert.setContentText("Data has been refreshed successfully!");
        alert.showAndWait();
    }

    @FXML
    private void handleTotalSales() {
        if (salesController != null) {
            salesController.updateTotalSales();
        }
    }

    @FXML
    private void handleCompare() {
        if (salesController != null) {
            salesController.showProductSelectionDialog();
        }
    }

    private void initializeSalesSection() {
        try {
            System.out.println("Initializing sales section...");
            
            if (salesChart == null || totalSalesLabel == null ||
                topProductLabel == null || salesDateLabel == null ||
                exportButton == null ||
                growthRateLabel == null || averageSalesLabel == null ||
                totalSalesButton == null || compareButton == null) {
                throw new RuntimeException("Sales components not found in FXML");
            }
            
            salesChart.setAnimated(false);
            ((CategoryAxis) salesChart.getXAxis()).setLabel("Month");
            ((NumberAxis) salesChart.getYAxis()).setLabel("Sales Volume");
            
            salesController = new SalesController();
            salesController.setMainController(this);
            salesController.setIdToken(idToken);
            salesController.initialize();
            
            salesController.injectComponents(
                salesChart,
                totalSalesLabel,
                topProductLabel,
                salesDateLabel,
                exportButton,
                growthRateLabel,
                averageSalesLabel,
                totalSalesButton,
                compareButton
            );
            
            System.out.println("Sales section initialization complete.");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error initializing sales section: " + e.getMessage());
        }
    }

    public void addRecentStockNotification(int stockCount, String description) {
        Platform.runLater(() -> {
            VBox notificationBox = new VBox();
            notificationBox.setPrefHeight(30);
            notificationBox.setMinHeight(30);
            notificationBox.setMaxHeight(30);
            notificationBox.setStyle("-fx-background-color: #0E1D47; -fx-background-radius: 7; -fx-padding: 1 1 1 1; -fx-margin: 0;");

            VBox.setMargin(notificationBox, new javafx.geometry.Insets(0, 0, 0, 0));

            HBox hBox = new HBox(8);
            hBox.setFillHeight(true);
            hBox.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 9 0 9;");

            String imagePath = "/images/stocks.png";
            ImageView imageView = createNotificationIcon(imagePath);

            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
            String formattedDate = currentTime.format(dateFormatter);

            String notificationText = stockCount + " stocks of " + description + " has arrived at the facility as of " + formattedDate;
            Label label = new Label(notificationText);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial';");

            hBox.getChildren().addAll(imageView, label);
            notificationBox.getChildren().add(hBox);

            recent.getChildren().add(0, notificationBox);

            if (recent.getParent() instanceof ScrollPane scrollPane) {
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(false);
                scrollPane.setPannable(true);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            }

            Connection connect = null;
            try {
                Object[] result = database_utility.update(
                    "INSERT INTO notifications_activities (activities) VALUES (?)",
                    notificationText
                );
                if (result != null) {
                    connect = (Connection) result[0];
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connect != null) {
                    database_utility.close(connect);
                }
            }
        });
    }

    public void addSoldStockNotification(int stockCount, String description) {
        Platform.runLater(() -> {
            VBox notificationBox = new VBox();
            notificationBox.setPrefHeight(30);
            notificationBox.setMinHeight(30);
            notificationBox.setMaxHeight(30);
            notificationBox.setStyle("-fx-background-color: #0E1D47; -fx-background-radius: 7; -fx-padding: 1 1 1 1; -fx-margin: 0;");

            VBox.setMargin(notificationBox, new javafx.geometry.Insets(0, 0, 0, 0));

            HBox hBox = new HBox(8);
            hBox.setFillHeight(true);
            hBox.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 9 0 9;");

            String imagePath = "/images/peso.png";
            ImageView imageView = createNotificationIcon(imagePath);

            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
            String formattedDate = currentTime.format(dateFormatter);

            String notificationText = stockCount + " stocks of " + description + " has been sold as of " + formattedDate;
            Label label = new Label(notificationText);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial';");

            hBox.getChildren().addAll(imageView, label);
            notificationBox.getChildren().add(hBox);

            recent.getChildren().add(0, notificationBox);

            if (recent.getParent() instanceof ScrollPane scrollPane) {
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(false);
                scrollPane.setPannable(true);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            }

            Connection connect = null;
            try {
                Object[] result = database_utility.update(
                    "INSERT INTO notifications_activities (activities) VALUES (?)",
                    notificationText
                );
                if (result != null) {
                    connect = (Connection) result[0];
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connect != null) {
                    database_utility.close(connect);
                }
            }
        });
    }

    @FXML
    private void handleEditButton() {
        try {
            int checkedCount = 0;
            Inventory_management_bin selectedItem = null;
            
            for (Inventory_management_bin item : inventory_table.getItems()) {
                if (item.getSelected()) {
                    checkedCount++;
                    selectedItem = item;
                }
            }
            
            if (checkedCount == 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select an item to edit.");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                return;
            } else if (checkedCount > 1) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select only one item to edit.");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addEditProduct/add-edit-product_form.fxml"));
            Parent editForm = loader.load();
            
            addeditproductController controller = loader.getController();
            controller.setDashboardController(this);
            controller.setItemToEdit(selectedItem);

            Scene scene = new Scene(editForm);
            scene.setFill(null);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle("Edit Product");
            stage.setScene(scene);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/intervein_logo_no_text.png")));
            
            Bounds paneBounds = right_pane.localToScreen(right_pane.getBoundsInLocal());
            stage.show();
            double centerX = paneBounds.getMinX() + (paneBounds.getWidth() / 2) - (stage.getWidth() / 2);
            double centerY = paneBounds.getMinY() + (paneBounds.getHeight() / 2) - (stage.getHeight() / 2);
            stage.setX(centerX);
            stage.setY(centerY);
            stage.toFront();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error loading edit form: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private ImageView createNotificationIcon(String iconPath) {
        Image icon;
        try {
            icon = new Image(getClass().getResource(iconPath).toExternalForm());
            if (icon.isError()) {
                throw new Exception("Icon failed to load");
            }
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + iconPath + ". Using fallback icon.");
            icon = new Image(getClass().getResource("/images/stocks.png").toExternalForm());
        }
        
        ImageView imageView = new ImageView(icon);
        imageView.setFitHeight(22);
        imageView.setFitWidth(22);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private void loadNotificationsFromDatabase() {
        System.out.println("TODO: Load notifications from Firebase");
        if (recent != null) {
            recent.getChildren().clear();
        }
    }
    
    @FXML
    private void handleGithubLink(MouseEvent event) {
        Label clickedLabel = (Label) event.getSource();
        String url = (String) clickedLabel.getUserData();
        if (hostServices != null) {
            hostServices.showDocument(url);
        } else {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;
                if (os.contains("win")) {
                    pb = new ProcessBuilder("cmd", "/c", "start", url);
                } else if (os.contains("mac")) {
                    pb = new ProcessBuilder("open", url);
                } else {
                    pb = new ProcessBuilder("xdg-open", url);
                }
                pb.start();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Could not open the link: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void updateStockNotifications() {
        ComboBox<String> stocksCombo = (ComboBox<String>) borderpane.lookup("#stocks");
        ComboBox<String> monthCombo = (ComboBox<String>) borderpane.lookup("#month");

        if (stocksCombo == null || monthCombo == null || recent1 == null) {
            return;
        }

        int threshold;
        try {
            threshold = Integer.parseInt(stocksCombo.getValue());
        } catch (NumberFormatException e) {
            threshold = 1000;
        }
        String selectedMonth = monthCombo.getValue().toLowerCase().substring(0, 3);

        recent1.getChildren().clear();

        Connection connect = null;
        try {
            String sql = String.format(
                "SELECT s.item_code, s.%s1 as stock_level, so.item_description, so.volume " +
                "FROM stock_onhand s " +
                "JOIN sale_offtake so ON s.item_code = so.item_code " +
                "WHERE s.%s1 <= ? " +
                "ORDER BY s.%s1 ASC",
                selectedMonth, selectedMonth, selectedMonth
            );

            Object[] result = database_utility.query(sql, threshold);
            if (result != null) {
                connect = (Connection) result[0];
                ResultSet rs = (ResultSet) result[1];

                while (rs.next()) {
                    int stockLevel = rs.getInt("stock_level");
                    String description = rs.getString("item_description");
                    int volume = rs.getInt("volume");

                    VBox notificationBox = new VBox();
                    notificationBox.setPrefHeight(30);
                    notificationBox.setMinHeight(30);
                    notificationBox.setMaxHeight(30);
                    notificationBox.setStyle("-fx-background-color: #0E1D47; -fx-background-radius: 7; -fx-padding: 1 1 1 1;");

                    HBox hBox = new HBox(8);
                    hBox.setFillHeight(true);
                    hBox.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 9 0 9;");

                    ImageView imageView = new ImageView(new Image(getClass().getResource("/images/stocks.png").toExternalForm()));
                    imageView.setFitHeight(22);
                    imageView.setFitWidth(22);
                    imageView.setPreserveRatio(true);

                    String notificationText = volume + " mL " + description + " has " + stockLevel + " stocks";
                    Label label = new Label(notificationText);
                    label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial';");

                    hBox.getChildren().addAll(imageView, label);
                    notificationBox.getChildren().add(hBox);

                    VBox.setMargin(notificationBox, new javafx.geometry.Insets(0, 0, 5, 0));

                    recent1.getChildren().add(notificationBox);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupSearch() {
        searchField.setPromptText("Search items...");
        searchField.setStyle("-fx-background-color: #081739; -fx-background-radius: 30; " +
                           "-fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.5);");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                inventory_management_query();
            } else {
                performSearch(newValue);
            }
        });
    }

    private void performSearch(String searchTerm) {
        System.out.println("TODO: Search inventory in Firebase for: " + searchTerm);
        if (inventory_management_table != null) {
            inventory_management_table.clear();
        }
    }

    public void addInventoryActionNotification(String action, String description) {
        Platform.runLater(() -> {
            VBox notificationBox = new VBox();
            notificationBox.setPrefHeight(30);
            notificationBox.setMinHeight(30);
            notificationBox.setMaxHeight(30);
            
            String imagePath;
            String notificationText;
            String backgroundColor = "#0E1D47";
            
            switch (action.toLowerCase()) {
                case "add":
                    imagePath = "/images/plus.png";
                    notificationText = "New product added: " + description;
                    break;
                case "edit":
                    imagePath = "/images/edit.png";
                    notificationText = "Product updated: " + description;
                    break;
                case "delete":
                    imagePath = "/images/trash.png";
                    notificationText = "Product deleted: " + description;
                    break;
                default:
                    imagePath = "/images/stocks.png";
                    notificationText = "Inventory action: " + description;
            }
            
            notificationBox.setStyle("-fx-background-color: " + backgroundColor + "; -fx-background-radius: 7; -fx-padding: 1 1 1 1; -fx-margin: 0;");
            VBox.setMargin(notificationBox, new javafx.geometry.Insets(0, 0, 0, 0));

            HBox hBox = new HBox(8);
            hBox.setFillHeight(true);
            hBox.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 9 0 9;");

            ImageView imageView = createNotificationIcon(imagePath);

            Label label = new Label(notificationText);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial';");

            hBox.getChildren().addAll(imageView, label);
            notificationBox.getChildren().add(hBox);

            recent.getChildren().add(0, notificationBox);

            if (recent.getParent() instanceof ScrollPane scrollPane) {
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(false);
                scrollPane.setPannable(true);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            }

            Connection connect = null;
            try {
                Object[] result = database_utility.update(
                    "INSERT INTO notifications_activities (activities) VALUES (?)",
                    notificationText
                );
                if (result != null) {
                    connect = (Connection) result[0];
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connect != null) {
                    database_utility.close(connect);
                }
            }

            if (forecastingController != null) {
                forecastingController.refreshProductList();
            }
        });
    }

    @FXML
    private void handleClearActivities() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Activities");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to clear all recent activities?");
        alert.initStyle(StageStyle.UNDECORATED);

        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {
                if (recent != null) {
                    recent.getChildren().clear();
                }

                Connection connect = null;
                try {
                    Object[] result = database_utility.update("DELETE FROM notifications_activities");
                    if (result != null) {
                        connect = (Connection) result[0];
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Failed to clear activities: " + e.getMessage());
                    errorAlert.initStyle(StageStyle.UNDECORATED);
                    errorAlert.showAndWait();
                } finally {
                    if (connect != null) {
                        database_utility.close(connect);
                    }
                }
            }
        });
    }

    public void showDashboard() {
        Platform.runLater(() -> {
            if (tabpane != null) {
                tabpane.getSelectionModel().select(0);
            }
            if (dashboardpane != null) {
                dashboardpane.setVisible(true);
            }
            TabSwitch(dashboardbutton, dashboardpane);
        });
    }
}