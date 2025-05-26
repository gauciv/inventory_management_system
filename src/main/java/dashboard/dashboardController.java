package dashboard;

import database.database_utility;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
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
    @FXML private Button settingsbutton;
    @FXML private AnchorPane settingspane;
    @FXML private Button helpbutton;
    @FXML private AnchorPane helppane;
    @FXML private Button activeButton;
    @FXML private TextField searchField;
    @FXML private TableView<SalesOfftake> myTable;
    @FXML private AnchorPane addFormContainer;
    @FXML private AnchorPane confirmationContainer;
    @FXML private VBox right_pane;
    @FXML private ChoiceBox<String> monthChoiceBox;

    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isFullscreen = false;
    private double prevWidth = 900;
    private double prevHeight = 450;
    private double prevX, prevY;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadTableData();
        
        // Add search field listener with more concise syntax
        searchField.textProperty().addListener((o) -> searchTable());
        
        styleActiveButton(dashboardbutton);

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
        TabSwitch(settingsbutton, settingspane);
        TabSwitch(helpbutton, helppane);

        searchField.prefWidthProperty().bind(inventorypane.widthProperty().divide(2).subtract(20));
        myTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Listen for size changes with more concise syntax
        inventorypane.widthProperty().addListener(o -> centerAddFormContainer());
        inventorypane.heightProperty().addListener(o -> centerAddFormContainer());
        inventorypane.widthProperty().addListener(o -> centerConfirmationContainer());
        inventorypane.heightProperty().addListener(o -> centerConfirmationContainer());


        Platform.runLater(() -> {
            centerAddFormContainer(); // initial center
        });

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/confirmation/confirmation_form.fxml"));
            Parent confirmationForm = loader.load();
            confirmationContainer.getChildren().setAll(confirmationForm);
            confirmationContainer.setVisible(false); // keep hidden initially
        } catch (IOException e) {
            e.printStackTrace();
        }

        monthChoiceBox.getItems().addAll(
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
        );

        monthChoiceBox.setValue("January");

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
                forecastingbutton, settingsbutton, helpbutton
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

        button.setOnAction(e -> {
            styleActiveButton(button);

            for (Tab tab : tabpane.getTabs()) {
                Node content = tab.getContent();

                if (content == pane) {
                    tabpane.getSelectionModel().select(tab);
                    return;
                }

                if (isDescendant(content, pane)) {
                    tabpane.getSelectionModel().select(tab);
                    return;
                }
            }

            System.out.println("No tab contains the given AnchorPane.");
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
    }

    @FXML
    private void handleAddButton() {

        try {
            // Load the full layout from FXML (with its own controller)
            Parent addForm = FXMLLoader.load(getClass().getResource("/addStocks/addstocks_form.fxml"));

            // Create a new Stage (window) to display the loaded layout
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT); // Make stage transparent and undecorated
            stage.setTitle("Add Stocks Form");

            // Create a scene with transparent fill
            Scene scene = new Scene(addForm);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);

            // Before showing, calculate position to center on right_pane
            // Get screen bounds of right_pane
            Bounds paneBounds = right_pane.localToScreen(right_pane.getBoundsInLocal());

            // Show the stage first to get its width and height
            stage.show();

            // Calculate centered position relative to right_pane
            double centerX = paneBounds.getMinX() + (paneBounds.getWidth() / 2) - (stage.getWidth() / 2);
            double centerY = paneBounds.getMinY() + (paneBounds.getHeight() / 2) - (stage.getHeight() / 2);

            // Set stage position
            stage.setX(centerX);
            stage.setY(centerY);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void handleSoldButton() {

        try {
            Parent addForm = FXMLLoader.load(getClass().getResource("/soldStocks/soldstock_form.fxml"));

            // Create a new Stage (window) to display the loaded layout
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT); // Make stage transparent and undecorated
            stage.setTitle("Sold Stocks Form");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

            // Create a scene with transparent fill
            Scene scene = new Scene(addForm);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);

            // Before showing, calculate position to center on right_pane
            // Get screen bounds of right_pane
            Bounds paneBounds = right_pane.localToScreen(right_pane.getBoundsInLocal());

            // Show the stage first to get its width and height
            stage.show();

            // Calculate centered position relative to right_pane
            double centerX = paneBounds.getMinX() + (paneBounds.getWidth() / 2) - (stage.getWidth() / 2);
            double centerY = paneBounds.getMinY() + (paneBounds.getHeight() / 2) - (stage.getHeight() / 2);

            // Set stage position
            stage.setX(centerX);
            stage.setY(centerY);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConfirmationButton() {

        try {
            Parent addForm = FXMLLoader.load(getClass().getResource("/confirmation/confirmation_form.fxml"));

            // Create a new Stage (window) to display the loaded layout
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT); // Make stage transparent and undecorated
            stage.setTitle("Sold Stocks Form");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

            // Create a scene with transparent fill
            Scene scene = new Scene(addForm);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);

            // Before showing, calculate position to center on right_pane
            // Get screen bounds of right_pane
            Bounds paneBounds = right_pane.localToScreen(right_pane.getBoundsInLocal());

            // Show the stage first to get its width and height
            stage.show();

            // Calculate centered position relative to right_pane
            double centerX = paneBounds.getMinX() + (paneBounds.getWidth() / 2) - (stage.getWidth() / 2);
            double centerY = paneBounds.getMinY() + (paneBounds.getHeight() / 2) - (stage.getHeight() / 2);

            // Set stage position
            stage.setX(centerX);
            stage.setY(centerY);

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

    private void setupTableColumns() {
        // Clear existing columns
        myTable.getColumns().clear();

        // Create and configure columns
        TableColumn<SalesOfftake, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<SalesOfftake, String> productNameColumn = new TableColumn<>("Product Name");
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<SalesOfftake, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<SalesOfftake, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<SalesOfftake, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Add columns to table one by one to avoid varargs warning
        myTable.getColumns().add(idColumn);
        myTable.getColumns().add(productNameColumn);
        myTable.getColumns().add(quantityColumn);
        myTable.getColumns().add(priceColumn);
        myTable.getColumns().add(dateColumn);
    }

    private void loadTableData() {
        try {
            Object[] result = database_utility.query("SELECT * FROM sales_offtake");
            if (result != null) {
                Connection conn = (Connection) result[0];
                ResultSet rs = (ResultSet) result[1];

                ObservableList<SalesOfftake> data = FXCollections.observableArrayList();

                while (rs.next()) {
                    SalesOfftake item = new SalesOfftake(
                        rs.getString("id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("date")
                    );
                    data.add(item);
                }

                myTable.setItems(data);

                // Close resources
                rs.close();
                database_utility.close(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText(null);
            alert.setContentText("Error loading data: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void searchTable() {
        String searchText = searchField.getText().toLowerCase();
        
        try {
            String query = "SELECT * FROM sales_offtake WHERE " +
                          "LOWER(id) LIKE ? OR " +
                          "LOWER(product_name) LIKE ? OR " +
                          "CAST(quantity AS CHAR) LIKE ? OR " +
                          "CAST(price AS CHAR) LIKE ? OR " +
                          "LOWER(date) LIKE ?";
            
            String searchPattern = "%" + searchText + "%";
            Object[] result = database_utility.query(query, 
                searchPattern, searchPattern, searchPattern, searchPattern, searchPattern);
            
            if (result != null) {
                Connection conn = (Connection) result[0];
                ResultSet rs = (ResultSet) result[1];

                ObservableList<SalesOfftake> filteredData = FXCollections.observableArrayList();

                while (rs.next()) {
                    SalesOfftake item = new SalesOfftake(
                        rs.getString("id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("date")
                    );
                    filteredData.add(item);
                }

                myTable.setItems(filteredData);

                // Close resources
                rs.close();
                database_utility.close(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Search Error");
            alert.setHeaderText(null);
            alert.setContentText("Error searching data: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void refreshTable() {
        loadTableData();
    }



}
