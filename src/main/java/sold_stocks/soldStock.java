package sold_stocks;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class soldStock {
    public void showPopup(Stage owner, AnchorPane inventoryPane, int itemCode, String description, int volume, String category, int salesOfftake, int stocksOnHand) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);

        stage.getIcons().add(new javafx.scene.image.Image(getClass().getResource("/images/logo.png").toExternalForm()));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/soldStocks/soldstock_form.fxml"));
        Scene scene = new Scene(loader.load(), 377, 432);
        
        // Get the controller and set the item data
        soldstocksController controller = loader.getController();
        controller.setItemData(itemCode, volume, category, salesOfftake, stocksOnHand);
        
        // Set the dashboard controller reference
        if (owner.getScene() != null && owner.getScene().getRoot() instanceof BorderPane) {
            BorderPane root = (BorderPane) owner.getScene().getRoot();
            dashboard.dashboardController dashboardController = (dashboard.dashboardController) root.getUserData();
            controller.setDashboardController(dashboardController);
        }
        
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Mark Stock as Sold");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);

        // Get the bounds of the inventory pane for centering
        Bounds paneBounds = inventoryPane.localToScreen(inventoryPane.getBoundsInLocal());
        
        // Show stage to get its dimensions
        stage.show();
        
        // Center the stage on the inventory pane
        double centerX = paneBounds.getMinX() + (paneBounds.getWidth() - stage.getWidth()) / 2;
        double centerY = paneBounds.getMinY() + (paneBounds.getHeight() - stage.getHeight()) / 2;
        
        // Set position and bring to front
        stage.setX(centerX);
        stage.setY(centerY);
        stage.toFront();
    }
}
