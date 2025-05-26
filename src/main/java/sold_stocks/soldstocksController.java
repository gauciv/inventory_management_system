package sold_stocks;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class soldstocksController {

    @FXML
    private Pane sold_pane;

    @FXML
    private void Exit() {
        if (sold_pane.getParent() instanceof Pane parent) {
            parent.getChildren().remove(sold_pane); // Only removes the subpane
        }
    }

}

