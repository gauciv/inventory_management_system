package login;

import database.database_utility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.sql.ResultSet;
import java.io.IOException;
import java.io.InputStream;

public class login_controller {

    @FXML
    private Pane login_pane;

    @FXML
    private PasswordField password;

    @FXML
    private TextField visiblePassword;

    @FXML private ImageView eyeimage;

    @FXML private Button eyebutton;

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




    @FXML private TextField username_field;

    @FXML
    private void login_button_clicked(){
        try {
            String username = username_field.getText(); // initialize the value inputed by the user in the username textfield
            String password_string = visiblePassword.getText(); // initialize the value inputed by the user in the password textfield

            Object[] result_from_query = database_utility.query("SELECT * FROM accounts WHERE username = ? AND password = ?", username, password_string);
            ResultSet result = (ResultSet) result_from_query[1]; // 'database_utility.query' returns two array object [0]Connection and [1]ResultSet(the database table)
            if (result.next()) {
                //This is only temporary while no next frame
                System.out.println(result.getString("first_name")+" "+result.getString("middle_initial")+" "+result.getString("last_name"));
            }
            else{
                //This is only temporary while no next frame
                System.out.println("Invalid username or password");
            }
        }catch(Exception e){
            //This is only temporary while no next frame
            System.out.println("SQL error");
            e.printStackTrace();
        }

    }
}
