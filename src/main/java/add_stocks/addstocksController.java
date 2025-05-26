package add_stocks;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class addstocksController {

    @FXML
    private Pane add_pane;

    @FXML
    private void Exit() {
        // Hide the pane first (optional if you remove it)
        // Get the current stage from the add_pane
        Stage stage = (Stage) add_pane.getScene().getWindow();

        // Close the stage (window)
        stage.close();
    }





}
