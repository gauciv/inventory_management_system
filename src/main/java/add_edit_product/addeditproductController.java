package add_edit_product;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;




public class addeditproductController {
    @FXML
    private Pane addedit_pane;
    private double xOffset = 0;
    private double yOffset = 0;

    public void initialize() {
        // Enable dragging of undecorated window
       addedit_pane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        addedit_pane.setOnMouseDragged(event -> {
            Stage stage = (Stage) addedit_pane.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

    }
}
