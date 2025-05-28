package login;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

public class IVDripAnimation extends Group {
    
    public IVDripAnimation() {
        createIVDrip();
    }
    
    private void createIVDrip() {
        // Create IV Bag
        Path ivBag = new Path();
        ivBag.getElements().addAll(
            new MoveTo(50, 20),
            new LineTo(100, 20),
            new LineTo(100, 70),
            new QuadCurveTo(100, 80, 75, 80),
            new QuadCurveTo(50, 80, 50, 70),
            new LineTo(50, 20)
        );
        ivBag.setFill(Color.rgb(135, 206, 235, 0.3));
        ivBag.setStroke(Color.rgb(135, 206, 235, 0.5));
        ivBag.setStrokeWidth(2);

        // Create tube
        Path tube = new Path();
        tube.getElements().addAll(
            new MoveTo(75, 80),
            new LineTo(75, 200)
        );
        tube.setStroke(Color.rgb(135, 206, 235, 0.5));
        tube.setStrokeWidth(2);

        // Create animated drops
        createAnimatedDrops();

        // Add all elements
        getChildren().addAll(ivBag, tube);
    }

    private void createAnimatedDrops() {
        for (int i = 0; i < 3; i++) {
            Circle drop = new Circle(75, 90 + (i * 30), 3, Color.rgb(135, 206, 235, 0.7));
            
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(drop.centerYProperty(), 90),
                    new KeyValue(drop.opacityProperty(), 0.7)),
                new KeyFrame(Duration.seconds(2), 
                    new KeyValue(drop.centerYProperty(), 200),
                    new KeyValue(drop.opacityProperty(), 0))
            );
            
            timeline.setDelay(Duration.seconds(i * 0.7));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
            
            getChildren().add(drop);
        }

        // Add liquid level animation in the bag
        Rectangle liquid = new Rectangle(52, 30, 46, 40);
        liquid.setFill(Color.rgb(135, 206, 235, 0.3));
        
        Timeline liquidTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(liquid.heightProperty(), 40)),
            new KeyFrame(Duration.seconds(2), 
                new KeyValue(liquid.heightProperty(), 35))
        );
        liquidTimeline.setAutoReverse(true);
        liquidTimeline.setCycleCount(Timeline.INDEFINITE);
        liquidTimeline.play();
        
        getChildren().add(liquid);
    }
} 