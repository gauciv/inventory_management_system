package dashboard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import database.database_utility;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
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
    @FXML private LineChart<String, Number> forecastChart;
    @FXML private ComboBox<String> forecastProductComboBox;
    @FXML private Label forecastAccuracyLabel;
    @FXML private Label forecastTrendLabel;
    @FXML private Label forecastRecommendationsLabel;
    @FXML private Label dateLabel;
    @FXML private Label dateTimeLabel;
    @FXML private LineChart<String, Number> salesChart;
    @FXML private BarChart<String, Number> salesBarChart;
    @FXML private AreaChart<String, Number> salesAreaChart;
    @FXML private Label totalSalesLabel;
    @FXML private Label topProductLabel;
    @FXML private Label salesDateLabel;
    @FXML private Label salesTimeLabel;
    @FXML private VBox recent;
    @FXML private ComboBox<String> forecastFormulaComboBox;
    @FXML private Label forecastPlaceholderLabel;
    @FXML private Button formulaHelpButton;

    @FXML private ComboBox<String> chartTypeComboBox;
    @FXML private ComboBox<String> compareProductComboBox;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private Button exportButton;
    @FXML private Label growthRateLabel;
    @FXML private Label averageSalesLabel;
    @FXML private Button totalSalesButton;
    @FXML private Button compareButton;


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

    @FXML
    public void initialize() {
        try {
            // Store controller reference in BorderPane's userData
            if (borderpane != null) {
                borderpane.setUserData(this);
            }
            
            // Initialize table first since other components may depend on it
            inventory_management_table = FXCollections.observableArrayList();
            if (inventory_table != null) {
                inventory_table.setItems(inventory_management_table);
            }
            
            setupTableView();
            setupWindowControls();
            setupFormContainers();
            styleActiveButton(dashboardbutton);
            
            // Initialize views after UI is set up
            Platform.runLater(() -> {
                initializeForecastingSection();
                initializeSalesSection();
                loadNotificationsFromDatabase();
            });
            startClock(); // Initialize the clock
            
            // Load initial data
            Platform.runLater(() -> {
                inventory_management_query();
            });
            
            // Initialize search functionality
            if (searchField != null) {
                setupSearch();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Initialization Error", "Failed to initialize the dashboard: " + e.getMessage());
        }

        if (monthComboBox != null) {
            monthComboBox.setStyle("-fx-prompt-text-fill: white; -fx-text-fill: white;");
            monthComboBox.setPromptText("Select a Month");
            // Auto-refresh inventory table when month changes
            monthComboBox.setOnAction(event -> inventory_management_query());
        }
    }

    private void initializeForecastingSection() {
        try {
            // Initialize forecasting controller
            forecastingController = new ForecastingController();
            
            // Configure chart before passing to controller
            if (forecastChart != null) {
                forecastChart.setAnimated(false); // Disable animations for better performance
                forecastChart.getXAxis().setLabel("Month");
                forecastChart.getYAxis().setLabel("Sales Volume");
            }
            
            // Initialize the forecasting controller with all UI components
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
            
            // Show error in a dialog
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
        // Add style classes to columns
        col_number.getStyleClass().add("col-number");
        col_select.getStyleClass().add("col-select");
        col_item_code.getStyleClass().add("col-item-code");
        col_item_des.getStyleClass().add("col-item-des");
        col_volume.getStyleClass().add("col-volume");
        col_category.getStyleClass().add("col-category");
        col_soh.getStyleClass().add("col-soh");
        col_sot.getStyleClass().add("col-sot");

        // Set column headers
        col_number.setText("#");
        col_select.setText("☐");  // Square box symbol
        col_item_code.setText("Item Code");
        col_item_des.setText("Product\nDescription");
        col_volume.setText("Volume");
        col_category.setText("Category");
        col_soh.setText("Stocks on\nHand");
        col_sot.setText("Sales\nOfftake");

        // Configure column alignment
        inventory_table.getColumns().forEach(column -> {
            column.setStyle("-fx-alignment: CENTER;");
        });

        // Special styling for select column header
        col_select.setStyle("-fx-alignment: CENTER; -fx-font-size: 16px;");


        // Set fixed column widths
        col_number.setPrefWidth(50);
        col_select.setPrefWidth(50);
        col_item_code.setPrefWidth(100);
        col_item_des.setPrefWidth(300);
        col_volume.setPrefWidth(100);
        col_category.setPrefWidth(150);
        col_soh.setPrefWidth(100);
        col_sot.setPrefWidth(100);

        // Set minimum widths to match preferred widths
        col_number.setMinWidth(col_number.getPrefWidth());
        col_select.setMinWidth(col_select.getPrefWidth());
        col_item_code.setMinWidth(col_item_code.getPrefWidth());
        col_item_des.setMinWidth(col_item_des.getPrefWidth());
        col_volume.setMinWidth(col_volume.getPrefWidth());
        col_category.setMinWidth(col_category.getPrefWidth());
        col_soh.setMinWidth(col_soh.getPrefWidth());
        col_sot.setMinWidth(col_sot.getPrefWidth());

        // Prevent column resizing
        inventory_table.getColumns().forEach(column -> {
            column.setResizable(false);
            column.setReorderable(false);
            column.setSortable(false);
        });


        // Make table responsive
        inventory_table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Bind table width to parent width with padding
        inventory_table.prefWidthProperty().bind(
            inventorypane.widthProperty().multiply(0.98)
        );
        
        // Bind table height to parent height with padding for other controls
        inventory_table.prefHeightProperty().bind(
            inventorypane.heightProperty().multiply(0.85)
        );

        // Initialize table columns with proper alignment
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
        
        // Setup checkbox column
        col_select.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        
        // Create CheckBoxes in each cell
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

        // Apply CSS styling
        inventory_table.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

        // Initialize data list and set it to table
        inventory_management_table = FXCollections.observableArrayList();
        inventory_table.setItems(inventory_management_table);
        
        // Load the inventory data
        inventory_management_query();

        // Force layout pass to ensure proper alignment
        Platform.runLater(() -> {
            inventory_table.refresh();
        });
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

        // Listen for size changes with more concise syntax
        inventorypane.widthProperty().addListener(o -> centerAddFormContainer());
        inventorypane.heightProperty().addListener(o -> centerAddFormContainer());
        inventorypane.widthProperty().addListener(o -> centerConfirmationContainer());
        inventorypane.heightProperty().addListener(o -> centerConfirmationContainer());

        Platform.runLater(() -> centerAddFormContainer());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/confirmation/confirmation_form.fxml"));
            Parent confirmationForm = loader.load();
            confirmationContainer.getChildren().setAll(confirmationForm);
            confirmationContainer.setVisible(false); // keep hidden initially
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
                // Focus gained: add text fill white, keep other styles
                searchField.setStyle(baseStyle + " -fx-text-fill: white;");
            } else {
                // Focus lost: remove the text fill override to default (black text)
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

        // If width/height are 0, fallback to prefWidth
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
            if (btn == selectedButton) {
                btn.setStyle(
                        "-fx-background-color: #2D3C7233;" +
                                "-fx-border-color: transparent transparent transparent #060D84;" +
                                "-fx-border-width: 0 0 0 2px;"
                );
                activeButton = btn;
            } else {
                btn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-width: 0;"
                );
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
        // Recenter form container after resize toggle
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

    public void TabSwitch(Button button, AnchorPane pane) {
        hideTabHeaders();
        button.setOnAction(__ -> {
            styleActiveButton(button);

            String tabText = button.getText().trim();
            for (Tab tab : tabpane.getTabs()) {
                if (tab.getText().equalsIgnoreCase(tabText) || 
                    tab.getText().equalsIgnoreCase(tabText.replace(" ", ""))) {
                    tabpane.getSelectionModel().select(tab);
                    if (pane != null) {
                        pane.setVisible(true);
                    }
                    return;
                }
            }
            System.out.println("No matching tab found for " + tabText);
        });
    }
    
    private boolean isDescendant(Node parent, Node child) {
        if (parent instanceof Parent) {
            for (Node node : ((Parent) parent).getChildrenUnmodifiable()) {
                if (node == child || isDescendant(node, child)) {
                    return true;
                }
            }
        }
        return false;
    }    @FXML
    private void handleAddButton() {
        try {
            // Count checked checkboxes in inventory table
            int checkedCount = 0;
            Inventory_management_bin selectedItem = null;
            
            for (Inventory_management_bin item : inventory_table.getItems()) {
                if (item.getSelected()) {
                    checkedCount++;
                    selectedItem = item;
                }
            }
            
            // Get the path and load appropriate FXML based on checkbox state
            String fxmlPath;
            String title;
            
            if (checkedCount > 1) {
                // Show error alert if multiple checkboxes are checked
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select only one item.");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                return;
            } else if (checkedCount == 1) {
                // Load addstocks form if exactly one checkbox is checked
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
                    // Pass dashboardController reference for auto-refresh
                    controller.setDashboardController(this);
                }
                Scene scene = new Scene(addForm);
                scene.setFill(null);
                Stage stage = new Stage();
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.setTitle(title);
                stage.setScene(scene);
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
                Bounds paneBounds = right_pane.localToScreen(right_pane.getBoundsInLocal());
                stage.show();
                double centerX = paneBounds.getMinX() + (paneBounds.getWidth() / 2) - (stage.getWidth() / 2);
                double centerY = paneBounds.getMinY() + (paneBounds.getHeight() / 2) - (stage.getHeight() / 2);
                stage.setX(centerX);
                stage.setY(centerY);
                stage.toFront();
                return;
            } else {
                // Load addproduct form if no checkbox is checked
                fxmlPath = "/addStocks/addproduct.fxml";
                title = "Add Product Form";
            }

            // Load the FXML and create scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent addForm = loader.load();
            
            // Get the controller and pass dashboard reference
            add_stocks.addproductController controller = loader.getController();
            if (controller == null) {
                throw new RuntimeException("Failed to get controller for add product form");
            }
            controller.setDashboardController(this);
            
            Scene scene = new Scene(addForm);
            scene.setFill(null); // Make scene background transparent
            
            // Create and configure stage
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            
            // Get screen bounds of right_pane for centering
            Bounds paneBounds = right_pane.localToScreen(right_pane.getBoundsInLocal());
            
            // Show stage to get its dimensions
            stage.show();
            
            // Center the stage on right_pane
            double centerX = paneBounds.getMinX() + (paneBounds.getWidth() / 2) - (stage.getWidth() / 2);
            double centerY = paneBounds.getMinY() + (paneBounds.getHeight() / 2) - (stage.getHeight() / 2);
            
            // Set position and bring to front
            stage.setX(centerX);
            stage.setY(centerY);
            stage.toFront();

        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert if loading fails
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load form: " + e.getMessage());
            alert.initStyle(StageStyle.UNDECORATED);
            alert.showAndWait();
        } catch (RuntimeException e) {
            e.printStackTrace();
            // Show error alert if controller initialization fails
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.initStyle(StageStyle.UNDECORATED);
            alert.showAndWait();
        }
    }    @FXML
    private void handleSoldButton() {
        try {
            // Count checked checkboxes in inventory table
            int checkedCount = 0;
            Inventory_management_bin selectedItem = null;
            
            for (Inventory_management_bin item : inventory_table.getItems()) {
                if (item.getSelected()) {
                    checkedCount++;
                    selectedItem = item;
                }
            }
            
            if (checkedCount == 0) {
                // Show error if no item is selected
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select an item to mark as sold.");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                return;
            } else if (checkedCount > 1) {
                // Show error alert if multiple checkboxes are checked
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select only one item.");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                return;
            }            soldStock dialog = new soldStock();
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
    }@FXML
    private void handleConfirmationButton() {
        try {
            // Count checked checkboxes in inventory table
            int checkedCount = 0;
            Inventory_management_bin selectedItem = null;
            
            for (Inventory_management_bin item : inventory_table.getItems()) {
                if (item.getSelected()) {
                    checkedCount++;
                    selectedItem = item;
                }
            }
            
            if (checkedCount == 0) {
                // Show error if no item is selected
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
            
            // Set up the deletion callback
            controller.setDeletionCallback(new confirmationController.DeletionCallback() {
                @Override
                public void onConfirmDeletion() {
                    // Remove from table
                    inventory_table.getItems().remove(itemToDelete);
                    
                    // Delete from database
                    try {
                        Connection connect = null;
                        try {
                            // First delete from stock_onhand (child table)
                            Object[] result = database_utility.update("DELETE FROM stock_onhand WHERE item_code = ?", itemToDelete.getItem_code());
                            if (result != null) {
                                connect = (Connection)result[0];
                                // Then delete from sale_offtake (parent table)
                                database_utility.update("DELETE FROM sale_offtake WHERE item_code = ?", itemToDelete.getItem_code());
                            }
                        } finally {
                            if (connect != null) {
                                database_utility.close(connect);
                            }
                        }
                        
                        // Refresh table data
                        inventory_management_query();
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Failed to delete item from database: " + e.getMessage());
                        alert.initStyle(StageStyle.UNDECORATED);
                        alert.showAndWait();
                    }
                }

                @Override
                public void onCancelDeletion() {
                    // Do nothing, dialog will be hidden by controller
                }
            });

            confirmationContainer.getChildren().setAll(confirmationForm);
            confirmationForm.setLayoutX(0);
            confirmationForm.setTranslateX(0);
            
            confirmationContainer.setVisible(true);
            confirmationContainer.toFront();

            // Wait for layout pass, then center
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

            // Wait for layout pass, then center
            Platform.runLater(() -> {
                confirmationContainer.applyCss();
                confirmationContainer.layout();
                centerConfirmationContainer();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //this section is for the inventory management tab

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


    // Helper method to get the selected month's column name
    public String getSelectedMonthColumn() {
        if (monthComboBox != null && monthComboBox.getValue() != null) {
            String month = monthComboBox.getValue().toLowerCase().substring(0, 3);
            return month;
        }
        return "dec"; // Default to December if no month is selected
    }

    // Make this method public so it can be called from addstocksController
    public void inventory_management_query() {
        Connection connect = null;
        try {
            String selectedMonth = getSelectedMonthColumn();
            String sql_query = String.format(
                "SELECT sale_offtake.item_code, item_description, volume, category, " +
                "sale_offtake.%s as sot, stock_onhand.%s1 as soh " +
                "FROM sale_offtake JOIN stock_onhand ON sale_offtake.item_code = stock_onhand.item_code",
                selectedMonth, selectedMonth
            );

            Object[] result_from_query = database_utility.query(sql_query);
            if (result_from_query != null) {
                connect = (Connection) result_from_query[0];
                ResultSet result = (ResultSet) result_from_query[1];

                ObservableList<Inventory_management_bin> items = FXCollections.observableArrayList();
                while (result.next()) {
                    items.add(new Inventory_management_bin(
                        result.getInt("item_code"),
                        result.getString("item_description"),
                        result.getInt("volume"),
                        result.getString("category"),
                        result.getInt("sot"),
                        result.getInt("soh")
                    ));
                }

                inventory_management_table.setAll(items);
                inventory_table.refresh();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connect != null) {
                database_utility.close(connect);
            }
        }
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedDate = currentTime.format(dateFormatter);
            String formattedTime = currentTime.format(timeFormatter);
            
            // Update dashboard date and time
            if (dateLabel != null) {
                dateLabel.setText("DATE: " + formattedDate + " | " + formattedTime);
            }
            
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }


    @FXML
    private void handleRefreshData() {
        // Clear existing table data
        if (inventory_management_table != null) {
            inventory_management_table.clear();
        }
        
        // Re-fetch data from database
        inventory_management_query();
        
        // Show success message
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
            
            // Make sure components are loaded
            if (salesChart == null || totalSalesLabel == null || 
                topProductLabel == null || salesDateLabel == null ||
                startDate == null || endDate == null || exportButton == null ||
                growthRateLabel == null || averageSalesLabel == null ||
                totalSalesButton == null || compareButton == null) {
                throw new RuntimeException("Sales components not found in FXML");
            }
            
            // Configure chart axes
            salesChart.setAnimated(false);
            ((CategoryAxis) salesChart.getXAxis()).setLabel("Month");
            ((NumberAxis) salesChart.getYAxis()).setLabel("Sales Volume");
            
            // Initialize sales controller
            salesController = new SalesController();
            
            // Initialize controller after injecting components
            salesController.initialize();
            
            // Inject all components
            salesController.injectComponents(
                salesChart, 
                totalSalesLabel, 
                topProductLabel, 
                salesDateLabel,
                startDate,
                endDate,
                exportButton,
                growthRateLabel,
                averageSalesLabel,
                totalSalesButton,
                compareButton
            );
            
            System.out.println("Sales section initialization complete.");
            
        } catch (Exception e) {
            System.err.println("Error initializing sales section: " + e.getMessage());
            e.printStackTrace();
            
            // Show user-friendly error
            if (totalSalesLabel != null) totalSalesLabel.setText("Error loading data");
            if (topProductLabel != null) topProductLabel.setText("Error loading data");
            if (growthRateLabel != null) growthRateLabel.setText("Growth Rate: N/A");
            if (averageSalesLabel != null) averageSalesLabel.setText("Avg. Monthly Sales: N/A");
        }
    }

    /**
     * Adds a notification to the recent VBox for newly arrived stocks.
     * @param stockCount The number of stocks added.
     * @param description The item description.
     */
    public void addRecentStockNotification(int stockCount, String description) {
        Platform.runLater(() -> {
            VBox notificationBox = new VBox();
            notificationBox.setPrefHeight(30);
            notificationBox.setMinHeight(30);
            notificationBox.setMaxHeight(30);
            notificationBox.setStyle("-fx-background-color: #0E1D47; -fx-background-radius: 7; -fx-padding: 1 1 1 1; -fx-margin: 0;");

            // Add margins using layout constraints
            VBox.setMargin(notificationBox, new javafx.geometry.Insets(0, 0, 0, 0));

            HBox hBox = new HBox(8);
            hBox.setFillHeight(true);
            hBox.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 9 0 9;"); // Adjusted padding to maintain content position

            ImageView imageView = new ImageView(new Image(getClass().getResource("/images/stocks.png").toExternalForm()));
            imageView.setFitHeight(22);
            imageView.setFitWidth(22);
            imageView.setPreserveRatio(true);

            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
            String formattedDate = currentTime.format(dateFormatter);

            String notificationText = stockCount + " stocks of " + description + " has arrived at the facility as of " + formattedDate;
            Label label = new Label(notificationText);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial';");

            hBox.getChildren().addAll(imageView, label);
            notificationBox.getChildren().add(hBox);

            // Add to the top of the VBox (most recent first)
            recent.getChildren().add(0, notificationBox);

            // If overflow, ensure parent VBox (recent) is scrollable and maintains its height
            if (recent.getParent() instanceof ScrollPane scrollPane) {
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(false);
                scrollPane.setPannable(true);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            }

            // Save to database
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

    /**
     * Adds a notification to the recent VBox for sold stocks.
     * @param stockCount The number of stocks sold.
     * @param description The item description.
     */
    public void addSoldStockNotification(int stockCount, String description) {
        Platform.runLater(() -> {
            VBox notificationBox = new VBox();
            notificationBox.setPrefHeight(30);
            notificationBox.setMinHeight(30);
            notificationBox.setMaxHeight(30);
            notificationBox.setStyle("-fx-background-color: #0E1D47; -fx-background-radius: 7; -fx-padding: 1 1 1 1; -fx-margin: 0;");

            // Add margins using layout constraints
            VBox.setMargin(notificationBox, new javafx.geometry.Insets(0, 0, 0, 0));

            HBox hBox = new HBox(8);
            hBox.setFillHeight(true);
            hBox.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 9 0 9;"); // Adjusted padding to maintain content position

            ImageView imageView = new ImageView(new Image(getClass().getResource("/images/peso.png").toExternalForm()));
            imageView.setFitHeight(22);
            imageView.setFitWidth(22);
            imageView.setPreserveRatio(true);

            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy");
            String formattedDate = currentTime.format(dateFormatter);

            String notificationText = stockCount + " stocks of " + description + " has been sold as of " + formattedDate;
            Label label = new Label(notificationText);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial';");

            hBox.getChildren().addAll(imageView, label);
            notificationBox.getChildren().add(hBox);

            // Add to the top of the VBox (most recent first)
            recent.getChildren().add(0, notificationBox);

            // If overflow, ensure parent VBox (recent) is scrollable and maintains its height
            if (recent.getParent() instanceof ScrollPane scrollPane) {
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(false);
                scrollPane.setPannable(true);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            }

            // Save to database
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
            // Count checked checkboxes in inventory table
            int checkedCount = 0;
            Inventory_management_bin selectedItem = null;
            
            for (Inventory_management_bin item : inventory_table.getItems()) {
                if (item.getSelected()) {
                    checkedCount++;
                    selectedItem = item;
                }
            }
            
            if (checkedCount == 0) {
                // Show error if no item is selected
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select an item to edit.");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                return;
            } else if (checkedCount > 1) {
                // Show error alert if multiple checkboxes are checked
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select only one item to edit.");
                alert.initStyle(StageStyle.UNDECORATED);
                alert.showAndWait();
                return;
            }

            // Create and show the edit form using add-edit-product_form.fxml
            add_edit_product.addeditproduct editForm = new add_edit_product.addeditproduct();
            editForm.showPopup((Stage) right_pane.getScene().getWindow(), inventorypane, selectedItem, this);

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to open edit form: " + e.getMessage());
            alert.initStyle(StageStyle.UNDECORATED);
            alert.showAndWait();
        }
    }
    private void loadNotificationsFromDatabase() {
        Connection connect = null;
        try {
            Object[] result = database_utility.query(
                "SELECT activities, timestamp FROM notifications_activities ORDER BY timestamp DESC"
            );
            if (result != null) {
                connect = (Connection) result[0];
                ResultSet rs = (ResultSet) result[1];

                while (rs.next()) {
                    String activity = rs.getString("activities");
                    
                    VBox notificationBox = new VBox();
                    notificationBox.setPrefHeight(30);
                    notificationBox.setMinHeight(30);
                    notificationBox.setMaxHeight(30);
                    notificationBox.setStyle("-fx-background-color: #0E1D47; -fx-background-radius: 7; -fx-padding: 1 1 1 1; -fx-margin: 0;");

                    VBox.setMargin(notificationBox, new javafx.geometry.Insets(0, 0, 0, 0));

                    HBox hBox = new HBox(8);
                    hBox.setFillHeight(true);
                    hBox.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 9 0 9;");

                    // Choose icon based on notification content
                    String imagePath = activity.contains("has been sold") ? "/images/peso.png" : "/images/stocks.png";
                    ImageView imageView = new ImageView(new Image(getClass().getResource(imagePath).toExternalForm()));
                    imageView.setFitHeight(22);
                    imageView.setFitWidth(22);
                    imageView.setPreserveRatio(true);

                    Label label = new Label(activity);
                    label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial';");

                    hBox.getChildren().addAll(imageView, label);
                    notificationBox.getChildren().add(hBox);

                    recent.getChildren().add(notificationBox);
                }

                // Configure scrolling if needed
                if (recent.getParent() instanceof ScrollPane scrollPane) {
                    scrollPane.setFitToWidth(true);
                    scrollPane.setFitToHeight(false);
                    scrollPane.setPannable(true);
                    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connect != null) {
                database_utility.close(connect);
            }
        }
    }
    
    @FXML
    private void handleGithubLink(MouseEvent event) {
        Label clickedLabel = (Label) event.getSource();
        String url = (String) clickedLabel.getUserData();
        if (hostServices != null) {
            hostServices.showDocument(url);
        } else {
            // Fallback using Runtime if HostServices is not available
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
                // Show error dialog
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Could not open the link: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void setupSearch() {
        // Set prompt text and style
        searchField.setPromptText("Search items...");
        searchField.setStyle("-fx-background-color: #081739; -fx-background-radius: 30; " +
                           "-fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.5);");

        // Add listener for real-time search
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                // If search field is empty, show all items
                inventory_management_query();
            } else {
                // Perform search with the new text
                performSearch(newValue);
            }
        });
    }

    private void performSearch(String searchTerm) {
        Connection connect = null;
        try {
            String selectedMonth = getSelectedMonthColumn();
            String sql_query = String.format(
                "SELECT sale_offtake.item_code, item_description, volume, category, " +
                "sale_offtake.%s as sot, stock_onhand.%s1 as soh " +
                "FROM sale_offtake JOIN stock_onhand ON sale_offtake.item_code = stock_onhand.item_code " +
                "WHERE LOWER(item_description) LIKE LOWER(?) OR " +
                "sale_offtake.item_code LIKE ? OR " +
                "LOWER(category) LIKE LOWER(?)",
                selectedMonth, selectedMonth
            );

            Object[] result_from_query = database_utility.query(sql_query, 
                "%" + searchTerm + "%",
                "%" + searchTerm + "%",
                "%" + searchTerm + "%"
            );

            if (result_from_query != null) {
                connect = (Connection) result_from_query[0];
                ResultSet result = (ResultSet) result_from_query[1];

                ObservableList<Inventory_management_bin> items = FXCollections.observableArrayList();
                while (result.next()) {
                    items.add(new Inventory_management_bin(
                        result.getInt("item_code"),
                        result.getString("item_description"),
                        result.getInt("volume"),
                        result.getString("category"),
                        result.getInt("sot"),
                        result.getInt("soh")
                    ));
                }

                inventory_management_table.setAll(items);
                inventory_table.refresh();

                // Show search results count
                String resultText = items.size() + " item" + (items.size() != 1 ? "s" : "") + " found";
                Tooltip tooltip = new Tooltip(resultText);
                searchField.setTooltip(tooltip);
                tooltip.show(searchField, 
                    searchField.localToScreen(searchField.getBoundsInLocal()).getMinX(),
                    searchField.localToScreen(searchField.getBoundsInLocal()).getMaxY());
                
                // Hide tooltip after 2 seconds
                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), 
                    ae -> tooltip.hide()));
                timeline.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Search Error", "Failed to perform search: " + e.getMessage());
        } finally {
            if (connect != null) {
                database_utility.close(connect);
            }
        }
    }
}
