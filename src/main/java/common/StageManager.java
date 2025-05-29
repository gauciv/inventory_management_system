package common;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class StageManager {
    private static final Image TASKBAR_ICON = new Image(StageManager.class.getResource("/images/intervein_logo_no_text.png").toExternalForm());

    public static void configureStage(Stage stage) {
        stage.getIcons().setAll(TASKBAR_ICON);
    }

    public static void setupDialog(Stage stage, Scene scene, String title) {
        stage.getIcons().setAll(TASKBAR_ICON);
        stage.setTitle(title);
        scene.setFill(null);
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
    }
}
