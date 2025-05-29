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
        
        // Get the controller and set HostServices
        dashboardController controller = loader.getController();
        controller.setHostServices(getHostServices());
        
        primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResource("/images/intervein_logo_no_text.png").toExternalForm()));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dashboard");
        primaryStage.setWidth(1075);
        primaryStage.setHeight(650);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
