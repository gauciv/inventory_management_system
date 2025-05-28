package login;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.ParallelTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class loadingController {
    @FXML
    private ProgressBar progressBar;
    
    @FXML
    private Label progressLabel;
    
    @FXML
    private Label loadingText;
    
    @FXML
    private Label checkMark;
    
    @FXML
    private StackPane progressContainer;

    private Timeline progressTimeline;
    private int currentStep = 0;
    private final String[] loadingMessages = {
        "Initializing...",
        "Loading data...",
        "Almost there..."
    };

    @FXML
    public void initialize() {
        setupProgressAnimation();
        startLoading();
    }

    private void setupProgressAnimation() {
        progressTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(progressBar.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(0.5), 
                new KeyValue(progressBar.progressProperty(), 0.33)),
            new KeyFrame(Duration.seconds(1.0), 
                new KeyValue(progressBar.progressProperty(), 0.66)),
            new KeyFrame(Duration.seconds(1.5), 
                new KeyValue(progressBar.progressProperty(), 1))
        );
        
        progressTimeline.setOnFinished(event -> onLoadingComplete());
        
        // Update progress label and loading text
        progressBar.progressProperty().addListener((obs, oldVal, newVal) -> {
            updateProgress(newVal.doubleValue());
        });
    }

    private void updateProgress(double progress) {
        int step = (int) Math.floor(progress * 3) + 1;
        if (step != currentStep && step <= 3) {
            currentStep = step;
            progressLabel.setText(currentStep + "/3");
            
            if (currentStep > 0 && currentStep <= loadingMessages.length) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), loadingText);
                fadeOut.setToValue(0);
                
                FadeTransition fadeIn = new FadeTransition(Duration.millis(150), loadingText);
                fadeIn.setToValue(1);
                
                fadeOut.setOnFinished(e -> {
                    loadingText.setText(loadingMessages[currentStep - 1]);
                    fadeIn.play();
                });
                
                fadeOut.play();
            }
        }
    }

    private void onLoadingComplete() {
        // Show completion animation
        FadeTransition fadeOutProgress = new FadeTransition(Duration.millis(200), progressContainer);
        fadeOutProgress.setToValue(0);
        
        FadeTransition fadeInCheck = new FadeTransition(Duration.millis(400), checkMark);
        fadeInCheck.setToValue(1);
        
        ParallelTransition transition = new ParallelTransition(fadeOutProgress, fadeInCheck);
        transition.setOnFinished(e -> {
            // Here you can trigger navigation to the next screen
            // For example: SceneManager.getInstance().showMainScreen();
        });
        
        transition.play();
    }

    public void startLoading() {
        progressBar.setProgress(0);
        currentStep = 0;
        loadingText.setText(loadingMessages[0]);
        checkMark.setOpacity(0);
        progressContainer.setOpacity(1);
        progressTimeline.play();
    }
} 