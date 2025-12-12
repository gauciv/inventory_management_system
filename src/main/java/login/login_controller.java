package login;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONObject;
import firebase.FirebaseConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class login_controller {

    // --- PASTE YOUR WEB API KEY HERE ---
    // Go to Firebase Console -> Project Settings -> General -> Web API Key
    private static final String WEB_API_KEY = "AIzaSyDzTB6ITybJlZRsIrMQVQ3cVgtQzw7fRj8";

    @FXML private Pane login_pane;
    @FXML private PasswordField password;
    @FXML private TextField visiblePassword;
    @FXML private ImageView eyeimage;
    @FXML private Button eyebutton;
    @FXML private TextField username_field;
    @FXML private Label errorLabel;

    private boolean isPasswordVisible = false;
    private double xOffset = 0;
    private double yOffset = 0;

    public static String idToken = null;

    @FXML
    public void initialize() {
        // Dragging logic
        login_pane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        login_pane.setOnMouseDragged(event -> {
            Stage stage = (Stage) login_pane.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        visiblePassword.setVisible(false);
        errorLabel.setVisible(false);

        // Enter key handlers
        username_field.setOnKeyPressed(event -> {
            errorLabel.setVisible(false);
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) login_button_clicked();
        });
        password.setOnKeyPressed(event -> {
            errorLabel.setVisible(false);
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) login_button_clicked();
        });
        visiblePassword.setOnKeyPressed(event -> {
            errorLabel.setVisible(false);
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) login_button_clicked();
        });
    }

    @FXML
    private void togglePasswordVisibility(MouseEvent event) {
        InputStream eyeStream = getClass().getResourceAsStream("/images/eye.png");
        InputStream eyeCloseStream = getClass().getResourceAsStream("/images/eyeclose.png");

        if (isPasswordVisible) {
            if (eyeStream != null) eyeimage.setImage(new Image(eyeStream));
            visiblePassword.setVisible(false);
            password.setVisible(true);
            password.setText(visiblePassword.getText());
        } else {
            if (eyeCloseStream != null) eyeimage.setImage(new Image(eyeCloseStream));
            visiblePassword.setText(password.getText());
            visiblePassword.setVisible(true);
            password.setVisible(false);
        }
        isPasswordVisible = !isPasswordVisible;
    }

    @FXML
    private void handleExit(MouseEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void handleMinimize(MouseEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void login_button_clicked() {
        String username = username_field.getText().trim();
        String password_string = isPasswordVisible ? visiblePassword.getText() : password.getText();

        if (username.isEmpty() || password_string.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        // Check for API Key placeholder
        if (WEB_API_KEY.equals("REPLACE_WITH_YOUR_WEB_API_KEY") || WEB_API_KEY.isEmpty()) {
            // Fallback: Try to get from FirebaseConfig if method exists, otherwise error
            try {
                // If you implemented getApiKey() in FirebaseConfig, uncomment below:
                // String configKey = FirebaseConfig.getApiKey(); 
                // if (configKey != null && !configKey.isEmpty()) {
                //     apiKeyToUse = configKey;
                // } else { throw new Exception("Key missing"); }
                
                showError("Configuration Error: Web API Key is missing.");
                return;
            } catch (Exception e) {
                showError("Configuration Error: Web API Key is missing.");
                return;
            }
        }

        showLoadingOverlay();

        new Thread(() -> {
            try {
                // Auth URL
                String urlStr = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + WEB_API_KEY;
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // JSON Body
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", username);
                jsonBody.put("password", password_string);
                jsonBody.put("returnSecureToken", true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    // Success
                    Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    scanner.close();

                    JSONObject jsonResponse = new JSONObject(response);
                    idToken = jsonResponse.getString("idToken");

                    Platform.runLater(() -> {
                        hideLoadingOverlay();
                        loadDashboard(username);
                    });
                } else {
                    // Failure
                    InputStream errorStream = conn.getErrorStream();
                    String errorResponse = "";
                    if (errorStream != null) {
                        Scanner scanner = new Scanner(errorStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
                        errorResponse = scanner.hasNext() ? scanner.next() : "";
                        scanner.close();
                    }

                    System.out.println("Firebase Error: " + errorResponse); // Debug print

                    String friendlyMessage = "Login failed.";
                    try {
                        if (!errorResponse.isEmpty()) {
                            JSONObject jsonError = new JSONObject(errorResponse);
                            if (jsonError.has("error")) {
                                String rawError = jsonError.getJSONObject("error").getString("message");
                                switch (rawError) {
                                    case "INVALID_PASSWORD":
                                        friendlyMessage = "Incorrect password.";
                                        break;
                                    case "EMAIL_NOT_FOUND":
                                        friendlyMessage = "Account not found.";
                                        break;
                                    case "USER_DISABLED":
                                        friendlyMessage = "Account disabled.";
                                        break;
                                    case "TOO_MANY_ATTEMPTS_TRY_LATER":
                                        friendlyMessage = "Too many attempts. Wait a moment.";
                                        break;
                                    case "INVALID_EMAIL":
                                        friendlyMessage = "Invalid email format.";
                                        break;
                                    case "MISSING_PASSWORD":
                                        friendlyMessage = "Missing password.";
                                        break;
                                    default:
                                        friendlyMessage = "Error: " + rawError;
                                        break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        friendlyMessage = "Error parsing response.";
                    }

                    final String msg = friendlyMessage;
                    Platform.runLater(() -> {
                        hideLoadingOverlay();
                        showError(msg);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    hideLoadingOverlay();
                    showError("Connection Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void loadDashboard(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            
            dashboard.dashboardController controller = loader.getController();
            controller.setIdToken(idToken);
            
            Stage dashboardStage = new Stage();
            dashboardStage.initStyle(StageStyle.UNDECORATED);
            Scene dashboardScene = new Scene(dashboardRoot);
            dashboardStage.setScene(dashboardScene);
            dashboardStage.getIcons().add(
                new Image(getClass().getResource("/images/intervein_logo_no_text.png").toExternalForm())
            );
            ((Stage) login_pane.getScene().getWindow()).close();
            dashboardStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Dashboard load failed.");
        }
    }

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