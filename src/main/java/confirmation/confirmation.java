package confirmation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;


public class confirmation {

    public void showPopup(Stage owner) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(owner); // Set the owner to avoid taskbar icon
        stage.initModality(Modality.WINDOW_MODAL); // Must be used with initOwner

        stage.getIcons().add(new javafx.scene.image.Image(getClass().getResource("/images/logo.png").toExternalForm()));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/confirmation/confirmation_form.fxml"));
        Scene scene = new Scene(loader.load(), 377, 432);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Add or Edit Product");
        stage.initStyle(StageStyle.TRANSPARENT); // or UNDECORATED if preferred
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.showAndWait();
    }
}
