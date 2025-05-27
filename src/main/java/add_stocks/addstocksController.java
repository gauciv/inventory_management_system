package add_stocks;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class addstocksController {    @FXML
    private Pane addPane;    
    
    @FXML
    private void handleExit() {
        // Get the window/stage the button is in and close it
        Stage stage = (Stage) addPane.getScene().getWindow();
        stage.close();
    }
}
