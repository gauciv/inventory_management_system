package dashboard;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import database.database_utility;

public class dashboardController {

    @FXML
    private Button minimizeButton;
    @FXML
    private Button resizeButton;
    @FXML
    private Button exitButton;
    @FXML
    private BorderPane borderpane;
    @FXML
    private TabPane tabpane;
    @FXML
    private Button dashboardbutton;
    @FXML
    private AnchorPane dashboardpane;
    @FXML
    private Button inventorybutton;
    @FXML
    private AnchorPane inventorypane;
    @FXML
    private Button forecastingbutton;
    @FXML
    private AnchorPane forecastingpane;
    @FXML
    private Button salesbutton;
    @FXML
    private AnchorPane salespane;
    @FXML
    private Button settingsbutton;
    @FXML
    private AnchorPane settingspane;
    @FXML
    private Button helpbutton;
    @FXML
    private AnchorPane helppane;
    @FXML
    private Button activeButton;
    @FXML
    private TextField searchField;
    @FXML
    private AnchorPane addFormContainer;
    @FXML
    private AnchorPane confirmationContainer;


    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isFullscreen = false;
    private double prevWidth = 900;
    private double prevHeight = 450;
    private double prevX, prevY;

    @FXML
    public void initialize() {
        // Initialize the window controls
        styleActiveButton(dashboardbutton);
        setupWindowControls();
        setupTableView();
        
        // Initialize form containers
        setupFormContainers();
    }
      private void setupTableView() {
        // Initialize table columns
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

        // Initialize data list and set it to table
        inventory_management_table = FXCollections.observableArrayList();
        inventory_table.setItems(inventory_management_table);
        
        // Load the inventory data
        inventory_management_query();
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
        TabSwitch(settingsbutton, settingspane);
        TabSwitch(helpbutton, helppane);
    }
    
    private void setupFormContainers() {
        searchField.prefWidthProperty().bind(inventorypane.widthProperty().divide(2).subtract(20));

        // Listen for inventorypane size changes to recenter containers
        inventorypane.widthProperty().addListener((_obs, _old, _new) -> centerAddFormContainer());
        inventorypane.heightProperty().addListener((_obs, _old, _new) -> centerAddFormContainer());
        inventorypane.widthProperty().addListener((_obs, _old, _new) -> centerConfirmationContainer());
        inventorypane.heightProperty().addListener((_obs, _old, _new) -> centerConfirmationContainer());

        Platform.runLater(() -> centerAddFormContainer());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/confirmation/confirmation_form.fxml"));
            Parent confirmationForm = loader.load();
            confirmationContainer.getChildren().setAll(confirmationForm);
            confirmationContainer.setVisible(false); // keep hidden initially
        } catch (IOException e) {
            e.printStackTrace();
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

        button.setOnAction(_event -> {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addStocks/addstocks_form.fxml"));
            loader.setController(this);
            Parent addForm = loader.load();

            addFormContainer.getChildren().setAll(addForm);
            addFormContainer.setVisible(true);
            addFormContainer.toFront();

            addFormContainer.layout();
            centerAddFormContainer();
            confirmationContainer.setVisible(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSoldButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/soldStocks/soldstock_form.fxml"));
            loader.setController(this);
            Parent soldForm = loader.load();

            addFormContainer.getChildren().setAll(soldForm);
            addFormContainer.setVisible(true);
            addFormContainer.toFront();

            addFormContainer.layout();
            centerAddFormContainer();
            confirmationContainer.setVisible(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConfirmationButton() {
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

            addFormContainer.setVisible(false);

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
    private TableView<Inventory_management_bin> inventory_table;    @FXML
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
    
    private ObservableList<Inventory_management_bin> inventory_management_table;


    void inventory_management_query() {
        Connection connect = null;
        try {
            String sql_query = "SELECT sale_offtake.item_code, item_description, volume, category, sale_offtake.dec, stock_onhand.dec1 " +
                    "FROM sale_offtake JOIN stock_onhand ON sale_offtake.item_code = stock_onhand.item_code";

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
                        result.getInt("dec"),
                        result.getInt("dec1")
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


}
