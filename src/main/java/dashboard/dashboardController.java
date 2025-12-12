package dashboard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import database.database_utility;
import firebase.FirestoreClient;
import firebase.FirebaseConfig;
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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.scene.control.Tooltip;
import forecasting.ForecastingController;
import confirmation.confirmationController;
import sold_stocks.soldStock;
import add_edit_product.addeditproductController;
import javafx.scene.layout.Region;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

public class dashboardController {
    @FXML private Button minimizeButton;
    @FXML private Button resizeButton;
    @FXML private Button exitButton;
    @FXML private BorderPane borderpane;
    @FXML private TabPane tabpane;
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
    @FXML private Label salesDateLabel;
    @FXML private AreaChart<String, Number> salesChart;
    @FXML private Label totalSalesLabel;
    @FXML private Label topProductLabel;
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
    private SalesController salesController;
    private javafx.application.HostServices hostServices;

    private String idToken;

    public void setIdToken(String idToken) {
        this.idToken = idToken;
        if (idToken != null) {
            loadDashboardData();
        }
    }

    public String getIdToken() {
        return this.idToken;
    }

    public void setHostServices(javafx.application.HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    public void initialize() {
        try {
            if (tabpane != null) {
                tabpane.lookupAll(".tab-header-area").forEach(node -> {
                    node.setVisible(false);
                    node.setManaged(false);
                });
            }

            if (dashboardpane != null) dashboardpane.setVisible(false);
            if (inventorypane != null) inventorypane.setVisible(false);
            if (salespane != null) salespane.setVisible(false);
            if (forecastingpane != null) forecastingpane.setVisible(false);
            if (helppane != null) helppane.setVisible(false);

            inventory_management_table = FXCollections.observableArrayList();
            
            if (borderpane != null) {
                borderpane.setUserData(this);
            }

            String currentMonth = java.time.LocalDate.now().getMonth().toString();
            currentMonth = currentMonth.substring(0, 1).toUpperCase() + currentMonth.substring(1).toLowerCase();

            setupTableView();
            setupWindowControls();
            setupFormContainers();
            
            if (monthComboBox != null) {
                monthComboBox.setStyle("-fx-prompt-text-fill: white; -fx-text-fill: white;");
                monthComboBox.setPromptText("Select a Month");
                monthComboBox.setValue(currentMonth);
                monthComboBox.setOnAction(event -> {
                    if (idToken != null) {
                        inventory_management_query();
                    }
                });
            }
            
            if (borderpane != null && borderpane.lookup("#month") != null) {
                ComboBox<String> dashboardMonthCombo = (ComboBox<String>) borderpane.lookup("#month");
                dashboardMonthCombo.setValue(currentMonth);
                dashboardMonthCombo.setOnAction(event -> updateStockNotifications());
            }

            if (borderpane != null && borderpane.lookup("#stocks") != null) {
                ComboBox<String> stocksCombo = (ComboBox<String>) borderpane.lookup("#stocks");
                stocksCombo.setOnAction(event -> updateStockNotifications());
            }
            
            startClock();
            setupNavigation();
            
            if (searchField != null) {
                setupSearch();
            }
            
            if (forecastRefreshButton != null) {
                forecastRefreshButton.setOnAction(e -> {
                    if (forecastingController != null) {
                        forecastingController.refreshProductList();
                    }
                });
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Initialization Error", "Failed to initialize the dashboard: " + e.getMessage());
        }
    }

    private void loadDashboardData() {
        initializeForecastingSection();
        initializeSalesSection();

        new Thread(() -> {
            try {
                Thread.sleep(500); 
                inventory_management_query(); // This calls updateStockNotifications internally
                loadNotificationsFromDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // --- ADDED MISSING METHOD HERE ---
    private void initializeSalesSection() {
        try {
            if (salesChart == null) return;
            
            salesController = new SalesController();
            salesController.setMainController(this);
            salesController.setIdToken(idToken);
            salesController.injectComponents(
                salesChart, totalSalesLabel, topProductLabel, salesDateLabel,
                exportButton, growthRateLabel, averageSalesLabel,
                totalSalesButton, compareButton
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ---------------------------------

    private void initializeForecastingSection() {
        try {
            forecastingController = new ForecastingController();
            forecastingController.setIdToken(idToken);
            
            if (forecastChart != null) {
                forecastChart.setAnimated(false);
                forecastChart.getXAxis().setLabel("Month");
                forecastChart.getYAxis().setLabel("Sales Volume");
                forecastChart.setLegendVisible(true);
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
            e.printStackTrace();
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
        if (inventory_table == null) return;

        inventory_table.setItems(inventory_management_table);
        
        // --- RESTORED STYLING AND TEXT ---
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
            column.setResizable(false);
            column.setReorderable(false);
            column.setSortable(false);
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

        inventory_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        if (inventorypane != null) {
            inventory_table.prefWidthProperty().bind(inventorypane.widthProperty().multiply(0.98));
            inventory_table.prefHeightProperty().bind(inventorypane.heightProperty().multiply(0.85));
        }
        // --- END RESTORED STYLING ---

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
                    // Check if item exists before setting selected property
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
        
        // This line is CRITICAL for applying CSS styles from /styles/style.css
        try {
             inventory_table.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        } catch (NullPointerException e) {
             System.err.println("Warning: Could not find /styles/style.css");
        }
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
    }
    
    private void setupNavigation() {
        dashboardbutton.setOnAction(e -> TabSwitch(dashboardbutton, dashboardpane));
        inventorybutton.setOnAction(e -> TabSwitch(inventorybutton, inventorypane));
        salesbutton.setOnAction(e -> TabSwitch(salesbutton, salespane));
        
        forecastingbutton.setOnAction(e -> {
            TabSwitch(forecastingbutton, forecastingpane);
            if (forecastingController != null) {
                forecastingController.refreshProductList();
            }
        });
        
        helpbutton.setOnAction(e -> TabSwitch(helpbutton, helppane));
        
        TabSwitch(dashboardbutton, dashboardpane);
    }
    
    private void setupFormContainers() {
        if(searchField != null) searchField.prefWidthProperty().bind(inventorypane.widthProperty().divide(2).subtract(20));
        if(inventorypane != null) {
            inventorypane.widthProperty().addListener(o -> centerAddFormContainer());
            inventorypane.heightProperty().addListener(o -> centerAddFormContainer());
            inventorypane.widthProperty().addListener(o -> centerConfirmationContainer());
            inventorypane.heightProperty().addListener(o -> centerConfirmationContainer());
        }

        Platform.runLater(() -> centerAddFormContainer());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/confirmation/confirmation_form.fxml"));
            Parent confirmationForm = loader.load();
            confirmationContainer.getChildren().setAll(confirmationForm);
            confirmationContainer.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(monthComboBox != null) {
            monthComboBox.getItems().addAll(
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
            );
        }
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
            isFullscreen = true;
        } else {
            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevWidth);
            stage.setHeight(prevHeight);
            isFullscreen = false;
        }
        stage.getScene().getRoot().requestLayout();
        Platform.runLater(this::centerAddFormContainer);
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
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
                public void onCancelDeletion() { }
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

    @FXML private TableView<Inventory_management_bin> inventory_table;
    @FXML private TableColumn<Inventory_management_bin, Integer> col_number;
    @FXML private TableColumn<Inventory_management_bin, Integer> col_item_code;
    @FXML private TableColumn<Inventory_management_bin, String> col_item_des;
    @FXML private TableColumn<Inventory_management_bin, Integer> col_volume;
    @FXML private TableColumn<Inventory_management_bin, String> col_category;
    @FXML private TableColumn<Inventory_management_bin, Integer> col_soh;
    @FXML private TableColumn<Inventory_management_bin, Integer> col_sot;
    @FXML private TableColumn<Inventory_management_bin, Boolean> col_select;
    private ObservableList<Inventory_management_bin> inventory_management_table;

    public void inventory_management_query() {
        if (inventory_management_table != null) inventory_management_table.clear();
        System.out.println("Fetching inventory data from Firestore...");
        
        new Thread(() -> {
            try {
                if (idToken == null) throw new Exception("No idToken set. User not authenticated.");
                String projectId = FirebaseConfig.getProjectId();
                String urlPath = "inventory?pageSize=1000";
                String response = FirestoreClient.getDocument(projectId, urlPath, idToken);
                JSONObject json = new JSONObject(response);
                org.json.JSONArray docs = json.optJSONArray("documents");
                
                java.util.List<Inventory_management_bin> tempList = new java.util.ArrayList<>();
                
                if (docs != null) {
                    for (int i = 0; i < docs.length(); i++) {
                        JSONObject doc = docs.getJSONObject(i);
                        JSONObject fields = doc.getJSONObject("fields");
                        
                        int item_code = fields.getJSONObject("item_code").getInt("integerValue");
                        String item_des = fields.getJSONObject("item_des").getString("stringValue");
                        int volume = fields.getJSONObject("volume").getInt("integerValue");
                        String category = fields.getJSONObject("category").getString("stringValue");
                        int sot = fields.getJSONObject("sot").getInt("integerValue");
                        int soh = fields.getJSONObject("soh").getInt("integerValue");
                        
                        tempList.add(new Inventory_management_bin(item_code, item_des, volume, category, sot, soh));
                    }
                }
                
                Platform.runLater(() -> {
                    inventory_management_table.setAll(tempList);
                    updateStockNotifications();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showErrorAlert("Firestore Error", "Failed to fetch inventory data: " + e.getMessage()));
            }
        }).start();
    }

    private void deleteInventoryItem(Inventory_management_bin itemToDelete) {
        new Thread(() -> {
            try {
                if (idToken == null) throw new Exception("No idToken set.");
                String projectId = FirebaseConfig.getProjectId();
                String documentId = String.valueOf(itemToDelete.getItem_code());
                FirestoreClient.deleteDocument(projectId, "inventory", documentId, idToken);
                
                Platform.runLater(() -> {
                    inventory_management_query();
                    if (forecastingController != null) {
                        // 1. Check if the deleted item was currently selected in the forecast combo box
                        if (forecastProductComboBox != null && 
                            forecastProductComboBox.getValue() != null &&
                            forecastProductComboBox.getValue().equals(itemToDelete.getItem_des())) {
                            
                            forecastProductComboBox.setValue(null); // Clear selection
                        }
                        
                        // 2. Refresh the list to remove the deleted product option
                        forecastingController.refreshProductList();
                    }
                });
                
                addInventoryActionNotification("delete", itemToDelete.getItem_des());
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showErrorAlert("Error", "Failed to delete item from Firestore: " + e.getMessage()));
            }
        }).start();
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            if (dateLabel != null) {
                dateLabel.setText("DATE: " + currentTime.format(dateFormatter) + " | " + currentTime.format(timeFormatter));
            }
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    @FXML
    private void handleRefreshData() {
        if (inventory_management_table != null) inventory_management_table.clear();
        inventory_management_query();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Refresh Complete");
        alert.setHeaderText(null);
        alert.setContentText("Data has been refreshed successfully!");
        alert.initStyle(StageStyle.UNDECORATED);
        alert.showAndWait();
    }

    @FXML
    public void handleTotalSales() {
        if (salesController != null) salesController.updateTotalSales();
    }

    @FXML
    private void handleCompare() {
        if (salesController != null) salesController.showProductSelectionDialog();
    }

    public void addRecentStockNotification(int stockCount, String description) {
        Platform.runLater(() -> {
            VBox notificationBox = new VBox();
            notificationBox.setStyle("-fx-background-color: #0E1D47; -fx-background-radius: 7; -fx-padding: 1 1 1 1;");
            
            HBox hBox = new HBox(8);
            hBox.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 9 0 9;");
            
            ImageView imageView = createNotificationIcon("/images/stocks.png");
            
            LocalDateTime now = LocalDateTime.now();
            String formattedDate = now.format(DateTimeFormatter.ofPattern("MMMM dd yyyy"));
            String text = stockCount + " stocks of " + description + " has arrived at the facility as of " + formattedDate;
            Label label = new Label(text);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial';");
            
            hBox.getChildren().addAll(imageView, label);
            notificationBox.getChildren().add(hBox);
            
            recent.getChildren().add(0, notificationBox);
        });
    }

    public void addSoldStockNotification(int stockCount, String description) {
        Platform.runLater(() -> {
            VBox notificationBox = new VBox();
            notificationBox.setStyle("-fx-background-color: #0E1D47; -fx-background-radius: 7; -fx-padding: 1 1 1 1;");
            HBox hBox = new HBox(8);
            hBox.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 9 0 9;");
            ImageView imageView = createNotificationIcon("/images/peso.png");
            String text = stockCount + " stocks of " + description + " has been sold as of " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd yyyy"));
            Label label = new Label(text);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial';");
            hBox.getChildren().addAll(imageView, label);
            notificationBox.getChildren().add(hBox);
            recent.getChildren().add(0, notificationBox);
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
            if (checkedCount != 1) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Error");
                alert.setContentText(checkedCount == 0 ? "Please select an item to edit." : "Please select only one item.");
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
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ImageView createNotificationIcon(String iconPath) {
        try {
            Image icon = new Image(getClass().getResource(iconPath).toExternalForm());
            ImageView imageView = new ImageView(icon);
            imageView.setFitHeight(22);
            imageView.setFitWidth(22);
            imageView.setPreserveRatio(true);
            return imageView;
        } catch (Exception e) {
            return new ImageView();
        }
    }

    private void loadNotificationsFromDatabase() {
        if (recent != null) recent.getChildren().clear();
    }
    
    @FXML
    private void handleGithubLink(MouseEvent event) {
        Label clickedLabel = (Label) event.getSource();
        String url = (String) clickedLabel.getUserData();
        if (hostServices != null) hostServices.showDocument(url);
    }

    private void updateStockNotifications() {
        if (recent1 == null) return;
        
        Platform.runLater(() -> {
            recent1.getChildren().clear();
            
            int threshold = 1000;
            ComboBox<String> stocksCombo = null;
            if (borderpane != null && borderpane.getScene() != null) {
                stocksCombo = (ComboBox<String>) borderpane.getScene().lookup("#stocks");
            }
            
            if (stocksCombo != null && stocksCombo.getValue() != null) {
                try {
                    threshold = Integer.parseInt(stocksCombo.getValue());
                } catch (NumberFormatException e) {
                    threshold = 1000;
                }
            }

            boolean hasCritical = false;
            
            if (inventory_management_table.isEmpty()) return;

            for (Inventory_management_bin item : inventory_management_table) {
                if (item.getSoh() <= threshold) {
                    hasCritical = true;
                    VBox alertBox = new VBox();
                    alertBox.setStyle("-fx-background-color: #3C0808; -fx-background-radius: 5; -fx-padding: 5; -fx-margin: 0 0 5 0;");
                    Label nameLabel = new Label(item.getItem_des());
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
                    Label stockLabel = new Label("Stocks: " + item.getSoh() + " / " + threshold);
                    stockLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 11px;");
                    alertBox.getChildren().addAll(nameLabel, stockLabel);
                    recent1.getChildren().add(alertBox);
                }
            }
            
            if (!hasCritical) {
                Label emptyLabel = new Label("No critical stocks.");
                emptyLabel.setStyle("-fx-text-fill: gray; -fx-padding: 10;");
                recent1.getChildren().add(emptyLabel);
            }
        });
    }
    
    private void setupSearch() {
        searchField.setPromptText("Search items...");
        searchField.setStyle("-fx-background-color: #081739; -fx-background-radius: 30; -fx-text-fill: white;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) inventory_management_query();
            else performSearch(newVal);
        });
    }

    private void performSearch(String searchTerm) {
        if (inventory_management_table != null) inventory_management_table.clear();
        // Placeholder for future implementation
    }

    public void addInventoryActionNotification(String action, String description) {
        // Placeholder
    }

    @FXML
    private void handleClearActivities() {
        if (recent != null) recent.getChildren().clear();
    }

    public void showDashboard() {
        Platform.runLater(() -> {
            if (tabpane != null) tabpane.getSelectionModel().select(0);
            if (dashboardpane != null) dashboardpane.setVisible(true);
            TabSwitch(dashboardbutton, dashboardpane);
        });
    }
}