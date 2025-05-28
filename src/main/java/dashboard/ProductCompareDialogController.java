package dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ProductCompareDialogController {
    @FXML private VBox productListVBox;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;
    @FXML private Label errorLabel;
    @FXML private Label trackerLabel;

    private List<String> products;
    private Consumer<List<String>> onConfirm;
    private Stage dialogStage;
    private final int MIN_SELECTION = 2;
    private final int MAX_SELECTION = 10;

    public void setProducts(List<String> products) {
        this.products = products;
        productListVBox.getChildren().clear();
        for (String product : products) {
            CheckBox cb = new CheckBox(product);
            cb.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> updateTracker());
            productListVBox.getChildren().add(cb);
        }
        updateTracker();
    }

    public void setOnConfirm(Consumer<List<String>> onConfirm) {
        this.onConfirm = onConfirm;
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        if (trackerLabel != null) trackerLabel.setText("");
        cancelButton.setOnAction(e -> {
            if (dialogStage != null) dialogStage.close();
        });
        confirmButton.setOnAction(e -> {
            List<String> selected = new ArrayList<>();
            for (javafx.scene.Node node : productListVBox.getChildren()) {
                if (node instanceof CheckBox cb && cb.isSelected()) {
                    selected.add(cb.getText());
                }
            }
            if (selected.size() < MIN_SELECTION) {
                showError("Please select at least 2 products.");
                return;
            }
            if (selected.size() > MAX_SELECTION) {
                showError("You can select a maximum of 10 products.");
                return;
            }
            if (onConfirm != null) onConfirm.accept(selected);
            if (dialogStage != null) dialogStage.close();
        });
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void updateTracker() {
        int checked = 0;
        for (javafx.scene.Node node : productListVBox.getChildren()) {
            if (node instanceof CheckBox cb && cb.isSelected()) checked++;
        }
        if (trackerLabel != null) {
            trackerLabel.setText("Checked: " + checked + " / 10");
            trackerLabel.setStyle("-fx-text-fill: #00b4ff; -fx-font-size: 13; -fx-font-weight: bold;");
        }
    }
} 