package add_edit_product;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import dashboard.Inventory_management_bin;

import java.io.IOException;

public class addeditproduct {
    public void showPopup(Stage owner, AnchorPane inventoryPane, Inventory_management_bin itemToEdit, dashboard.dashboardController dashboardController) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);

        stage.getIcons().add(new javafx.scene.image.Image(getClass().getResource("/images/intervein_logo_no_text.png").toExternalForm()));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/addEditProduct/add-edit-product_form.fxml"));
        Scene scene = new Scene(loader.load(), 377, 432);
        
        // Get the controller and set up the data
        addeditproductController controller = loader.getController();
        controller.setDashboardController(dashboardController);
        controller.setItemToEdit(itemToEdit);
        
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Edit Product");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);

        // Center the stage on the inventory pane
        Bounds paneBounds = inventoryPane.localToScreen(inventoryPane.getBoundsInLocal());
        stage.show();
        double centerX = paneBounds.getMinX() + (paneBounds.getWidth() / 2) - (stage.getWidth() / 2);
        double centerY = paneBounds.getMinY() + (paneBounds.getHeight() / 2) - (stage.getHeight() / 2);
        stage.setX(centerX);
        stage.setY(centerY);
        stage.toFront();
    }
}
