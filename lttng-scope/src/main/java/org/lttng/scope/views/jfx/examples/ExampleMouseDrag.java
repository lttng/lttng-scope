package org.lttng.scope.views.jfx.examples;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * @web http://java-buddy.blogspot.com/
 */
public class ExampleMouseDrag extends Application {

    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;

    @Override
    public void start(Stage primaryStage) {
        if (primaryStage == null) {
            return;
        }

        //Create Circles
        Circle circleRed = new Circle(50.0, Color.RED);
        circleRed.setCursor(Cursor.HAND);
        circleRed.setOnMousePressed(circleOnMousePressedEventHandler);
        circleRed.setOnMouseDragged(circleOnMouseDraggedEventHandler);

        Circle circleGreen = new Circle(50.0, Color.GREEN);
        circleGreen.setCursor(Cursor.MOVE);
        circleGreen.setCenterX(150);
        circleGreen.setCenterY(150);
        circleGreen.setOnMousePressed(circleOnMousePressedEventHandler);
        circleGreen.setOnMouseDragged(circleOnMouseDraggedEventHandler);

        Circle circleBlue = new Circle(50.0, Color.BLUE);
        circleBlue.setCursor(Cursor.CROSSHAIR);
        circleBlue.setTranslateX(300);
        circleBlue.setTranslateY(100);
        circleBlue.setOnMousePressed(circleOnMousePressedEventHandler);
        circleBlue.setOnMouseDragged(circleOnMouseDraggedEventHandler);

        Group root = new Group();
        root.getChildren().addAll(circleRed, circleGreen, circleBlue);

        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 400,350));

        primaryStage.setTitle("java-buddy");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private EventHandler<MouseEvent> circleOnMousePressedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {
            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();
            orgTranslateX = ((Circle)(t.getSource())).getTranslateX();
            orgTranslateY = ((Circle)(t.getSource())).getTranslateY();
        }
    };

    private EventHandler<MouseEvent> circleOnMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {
            double offsetX = t.getSceneX() - orgSceneX;
            double offsetY = t.getSceneY() - orgSceneY;
            double newTranslateX = orgTranslateX + offsetX;
            double newTranslateY = orgTranslateY + offsetY;

            ((Circle)(t.getSource())).setTranslateX(newTranslateX);
            ((Circle)(t.getSource())).setTranslateY(newTranslateY);
        }
    };
}