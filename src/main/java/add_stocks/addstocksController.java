package add_stocks;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class addstocksController {

    @FXML
    private Pane add_pane;

    @FXML
    private void Exit() {
        if (add_pane.getParent() instanceof Pane parent) {
            parent.getChildren().remove(add_pane);
        }
    }
}
