package confirmation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class confirmation extends Application {
    @Override
    public void start(@NotNull Stage stage) throws IOException {
        // Set icon (taskbar, window bar)
        stage.getIcons().add(new javafx.scene.image.Image(getClass().getResource("/images/logo.png").toExternalForm()));
        FXMLLoader fxmlLoader = new FXMLLoader(confirmation.class.getResource("/confirmation/confirmation_form.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 338, 180);
        scene.setFill(Color.TRANSPARENT); // removes white background
        stage.setTitle("confirmation page");
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT); // <--- this removes the top bar
        stage.show();

        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("JavaFX version: " + System.getProperty("javafx.version"));
    }

    public static void main(String[] args) {
        launch();
    }
}
