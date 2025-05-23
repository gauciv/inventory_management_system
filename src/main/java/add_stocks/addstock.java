package add_stocks;

import dashboard.dashboardController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static javafx.application.Application.launch;

public class addstock extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/add-stocks/add_stocks.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResource("/images/logo.png").toExternalForm()));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Add Stocks");
        primaryStage.setWidth(417);
        primaryStage.setHeight(480);
        primaryStage.show();
        addstocksController controller = loader.getController();

    }

    public static void main(String[] args) {
        launch(args);
    }
}


