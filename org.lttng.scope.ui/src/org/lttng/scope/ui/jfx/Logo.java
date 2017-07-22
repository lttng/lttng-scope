package org.lttng.scope.ui.jfx;

import org.eclipse.jdt.annotation.Nullable;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Logo extends Application {

    private static final String BACKGROUND_STYLE = "-fx-background-color: rgba(255, 255, 255, 255);"; //$NON-NLS-1$

    private static final Color LTTNG_PURPLE = Color.web("#996ABC"); //$NON-NLS-1$
    private static final Color LTTNG_LIGHT_BLUE = Color.web("#C3DEF4"); //$NON-NLS-1$

    @Override
    public void start(@Nullable Stage stage) throws Exception {
        if (stage == null) {
            return;
        }

        Rectangle clipRect1 = new Rectangle(-130, -130, 115, 115);
        Rectangle clipRect2 = new Rectangle(15, -130, 115, 115);
        Rectangle clipRect3 = new Rectangle(-130, 15, 115, 115);
        Rectangle clipRect4 = new Rectangle(15, 15, 115, 115);
        Group clip = new Group(clipRect1, clipRect2, clipRect3, clipRect4);

        Circle spinnanCircle = new Circle(100);
        spinnanCircle.setFill(null);
        spinnanCircle.setStrokeWidth(30);
        spinnanCircle.setStroke(LTTNG_PURPLE);
        spinnanCircle.setClip(clip);

        Circle magCircle = new Circle(60);
        magCircle.setFill(null);
        magCircle.setStrokeWidth(25);
        magCircle.setStroke(LTTNG_LIGHT_BLUE);

        Rectangle magHandle = new Rectangle(-12.5, 60, 25, 110);
        magHandle.setFill(LTTNG_LIGHT_BLUE);

        Group mag = new Group(magCircle, magHandle);

        Group root = new Group(spinnanCircle, mag);
        root.setRotate(30);
        root.relocate(0, 0);

        Pane pane = new Pane(root);
        pane.setStyle(BACKGROUND_STYLE);

        RotateTransition spinnan = new RotateTransition(Duration.seconds(4), spinnanCircle);
        spinnan.setByAngle(360);
        spinnan.setCycleCount(Animation.INDEFINITE);
        spinnan.setInterpolator(Interpolator.LINEAR);

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();

        spinnan.play();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
