package dashboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class dashboard extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResource("/images/logo.png").toExternalForm()));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dashboard");
        primaryStage.setWidth(1050);
        primaryStage.setHeight(650);
        primaryStage.show();



    }

    public static void main(String[] args) {
        launch(args);
    }
}
