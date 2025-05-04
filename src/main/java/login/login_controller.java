package login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class login_controller {

    @FXML
    private Pane login_pane;

    @FXML
    private PasswordField password;

    @FXML
    private TextField visiblePassword;

    @FXML
    private ImageView eyeimage;

    @FXML
    private Button eyebutton;

    private boolean isPasswordVisible = false;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // Enable dragging of undecorated window
        login_pane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        login_pane.setOnMouseDragged(event -> {
            Stage stage = (Stage) login_pane.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Hide visible password field initially
        visiblePassword.setVisible(false);
    }

    @FXML
    private void togglePasswordVisibility(MouseEvent event) {
        InputStream eyeStream = getClass().getResourceAsStream("/images/eye.png");
        InputStream eyeCloseStream = getClass().getResourceAsStream("/images/eyeclose.png");

        if (eyeStream == null || eyeCloseStream == null) {
            System.out.println("Error: Image files not found.");
            return;
        }

        if (isPasswordVisible) {
            eyeimage.setImage(new Image(eyeStream));
            visiblePassword.setVisible(false);
            password.setVisible(true);
            password.setText(visiblePassword.getText());
        } else {
            eyeimage.setImage(new Image(eyeCloseStream));
            visiblePassword.setText(password.getText());
            visiblePassword.setVisible(true);
            password.setVisible(false);
        }

        isPasswordVisible = !isPasswordVisible;
    }

    @FXML
    private void handleExit(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleMinimize(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

}
