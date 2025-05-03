package login;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.io.InputStream;


public class login_controller {

    @FXML
    private Pane login_pane; // or whatever type you're using

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        login_pane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        login_pane.setOnMouseDragged(event -> {
            login_pane.getScene().getWindow().setX(event.getScreenX() - xOffset);
            login_pane.getScene().getWindow().setY(event.getScreenY() - yOffset);
        });
    }

/*
    @FXML
    private ImageView eyeimage;  // ImageView for the eye icon
    @FXML
    private PasswordField password;  // Password field to show/hide text
    @FXML
    private javafx.scene.control.Button eyebutton;  // Button to toggle the image and password visibility

    private boolean isPasswordVisible = false;  // Boolean to track the visibility of the password

    // Method to toggle eye image and password visibility
    @FXML
    private void togglePasswordVisibility(MouseEvent event) {
        InputStream eyeStream = getClass().getResourceAsStream("/images/eye.png");
        InputStream eyeCloseStream = getClass().getResourceAsStream("/images/eyeclose.png");

        if (eyeStream == null || eyeCloseStream == null) {
            System.out.println("Error: Image files not found.");
            return;  // Return early if images are not found
        }

        if (isPasswordVisible) {
            // Change image to eye.png (password hidden)
            eyeimage.setImage(new Image(eyeStream));

            // Hide the password as dots (set password field text back to masked version)
            password.setText(password.getText());  // Ensure it's masked
            password.setPromptText("");  // Clear prompt text
        } else {
            // Change image to eyeclose.png (password visible)
            eyeimage.setImage(new Image(eyeCloseStream));

            // Show password as plain text
            password.setText(password.getText());  // Show password text
            password.setPromptText("");  // Clear prompt text
        }

        // Toggle the state (visibility of password)
        isPasswordVisible = !isPasswordVisible;
    }


*/

    @FXML
    private ImageView eyeimage;  // ImageView for the eye icon
    @FXML
    private PasswordField password;  // PasswordField for masked text
    @FXML
    private TextField visiblePassword;  // TextField for visible text
    @FXML
    private javafx.scene.control.Button eyebutton;  // Button to toggle the image and password visibility

    private boolean isPasswordVisible = false;  // Boolean to track the visibility of the password

    // Method to toggle eye image and password visibility
    @FXML
    private void togglePasswordVisibility(MouseEvent event) {
        InputStream eyeStream = getClass().getResourceAsStream("/images/eye.png");
        InputStream eyeCloseStream = getClass().getResourceAsStream("/images/eyeclose.png");

        if (eyeStream == null || eyeCloseStream == null) {
            System.out.println("Error: Image files not found.");
            return;  // Return early if images are not found
        }

        if (isPasswordVisible) {
            // Change image to eye.png (password hidden)
            eyeimage.setImage(new Image(eyeStream));

            // Hide the visible password and show the masked password field
            visiblePassword.setVisible(false);
            password.setVisible(true);

            // Set the visible password field's text to the password
            password.setText(visiblePassword.getText());
        } else {
            // Change image to eyeclose.png (password visible)
            eyeimage.setImage(new Image(eyeCloseStream));

            // Hide the masked password field and show the visible password field
            password.setVisible(false);
            visiblePassword.setVisible(true);

            // Set the visible password field's text to the password
            visiblePassword.setText(password.getText());
        }

        // Toggle the state (visibility of password)
        isPasswordVisible = !isPasswordVisible;
    }






}

