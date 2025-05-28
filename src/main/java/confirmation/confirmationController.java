package confirmation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.StageStyle;

public class confirmationController {

    @FXML
    private AnchorPane confirmation_pane;
    
    private DeletionCallback deletionCallback;
    
    public interface DeletionCallback {
        void onConfirmDeletion();
        void onCancelDeletion();
    }
    
    public void setDeletionCallback(DeletionCallback callback) {
        this.deletionCallback = callback;
    }

    @FXML
    private void handleNextButton() {
        if (deletionCallback != null) {
            deletionCallback.onConfirmDeletion();
        }
        hideConfirmation();
    }

    @FXML
    private void handleUndoButton() {
        if (deletionCallback != null) {
            deletionCallback.onCancelDeletion();
        }
        hideConfirmation();
    }

    @FXML
    private void Exit() {
        hideConfirmation();
    }
    
    private void hideConfirmation() {
        if (confirmation_pane.getParent() instanceof Pane parent) {
            parent.setVisible(false);
        }
    }
}

