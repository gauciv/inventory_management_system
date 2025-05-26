package confirmation;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class confirmationController {

    @FXML
    private Pane confirmation_pane;

    @FXML
    private void Exit() {
        if (confirmation_pane.getParent() instanceof Pane parent) {
            parent.getChildren().remove(confirmation_pane);
        }
    }
}

