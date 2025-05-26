package sold_stocks;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class soldstocksController {
    @FXML
    private Pane sold_pane;

    @FXML
    private void Exit() {
        // Hide the pane first (optional if you remove it)
        // Get the current stage from the add_pane
        Stage stage = (Stage) sold_pane.getScene().getWindow();

        // Close the stage (window)
        stage.close();
    }

}
