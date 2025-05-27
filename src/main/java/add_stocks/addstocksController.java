package add_stocks;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class addstocksController {
    @FXML
    private Pane addPane;
    @FXML @SuppressWarnings("all")
    public TextField text_field1;
    @FXML
    public TextField textfield2;
    @FXML @SuppressWarnings("all")
    public TextField text_field3;
    @FXML @SuppressWarnings("all")
    public TextField text_field4;

    @FXML
    private void handleExit() {
        // Get the window/stage the button is in and close it
        Stage stage = (Stage) addPane.getScene().getWindow();
        stage.close();
    }
}
