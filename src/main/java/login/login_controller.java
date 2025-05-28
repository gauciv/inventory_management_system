package login;

import database.database_utility;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;

public class login_controller {

    @FXML private Pane login_pane;
    @FXML private PasswordField password;
    @FXML private TextField visiblePassword;
    @FXML private ImageView eyeimage;
    @FXML private Button eyebutton;
    @FXML private TextField username_field;

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

        // Add Enter key handler for both password fields and the username field
        username_field.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> login_button_clicked();
            }
        });

        password.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> login_button_clicked();
            }
        });

        visiblePassword.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> login_button_clicked();
            }
        });
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
            // Hide password (switch to PasswordField)
            eyeimage.setImage(new Image(eyeStream));
            visiblePassword.setVisible(false);
            password.setVisible(true);
            password.setText(visiblePassword.getText());
        } else {
            // Show password (switch to TextField)
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

    @FXML
    private void login_button_clicked() {
        String username = username_field.getText().trim();
        String password_string = isPasswordVisible ? visiblePassword.getText() : password.getText();

        if (username.isEmpty() || password_string.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Login Error", "Please enter both username and password.");
            return;
        }

        try {
            Object[] result_from_query = database_utility.query(
                "SELECT * FROM accounts WHERE username = ? AND password = ?", 
                username, password_string
            );

            if (result_from_query == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database. Please try again.");
                return;
            }

            Connection conn = (Connection) result_from_query[0];
            ResultSet result = (ResultSet) result_from_query[1];

            try {
                if (result.next()) {
                    // Login successful
                    String fullName = result.getString("first_name") + " " + 
                                    result.getString("middle_initial") + " " + 
                                    result.getString("last_name");
                    String usernameFromDb = result.getString("username");

                    // Show loading overlay (non-blocking)
                    showLoadingOverlay();

                    // Load the dashboard in a background thread
                    new Thread(() -> {
                        try {
                            // Simulate loading time (optional, remove in production)
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {}
                        javafx.application.Platform.runLater(() -> {
                            loadDashboard(usernameFromDb);
                            hideLoadingOverlay();
                        });
                    }).start();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", 
                             "Invalid username or password.\nPlease try again.");
                }
            } finally {
                // Close the database resources
                if (result != null) result.close();
                if (conn != null) database_utility.close(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "System Error", 
                     "An error occurred while processing your request.\n" + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Make alert match the application's style
        alert.initStyle(StageStyle.UNDECORATED);
        alert.showAndWait();
    }

    private void loadDashboard(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            
            // Get the controller and pass any necessary data
            // dashboardController controller = loader.getController();
            // controller.setUserData(username);
            
            // Create new scene and stage for dashboard
            Stage dashboardStage = new Stage();
            dashboardStage.initStyle(StageStyle.UNDECORATED);
            
            Scene dashboardScene = new Scene(dashboardRoot);
            dashboardStage.setScene(dashboardScene);
            
            // Set the icon for the dashboard window
            dashboardStage.getIcons().add(
                new Image(getClass().getResource("/images/logo.png").toExternalForm())
            );
            
            // Close the login window
            ((Stage) login_pane.getScene().getWindow()).close();
            
            // Show the dashboard
            dashboardStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                     "Could not load the dashboard.\n" + e.getMessage());
        }
    }

    // --- Loading overlay logic ---
    private Stage loadingStage;
    private void showLoadingOverlay() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login/loading.fxml"));
            Parent loadingRoot = loader.load();
            loadingStage = new Stage(StageStyle.TRANSPARENT);
            loadingStage.initOwner(login_pane.getScene().getWindow());
            loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.setAlwaysOnTop(true);
            Scene scene = new Scene(loadingRoot);
            scene.setFill(null);
            loadingStage.setScene(scene);
            loadingStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void hideLoadingOverlay() {
        if (loadingStage != null) {
            loadingStage.close();
            loadingStage = null;
        }
    }
}
