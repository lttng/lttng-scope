package org.lttng.scope.ui.jfx.examples;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.ui.jfx.Arrow;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ArrowExample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(@Nullable Stage primaryStage) throws Exception {
        if (primaryStage == null) {
            return;
        }

        Pane root = new Pane();
        Arrow arrow = new Arrow();
        arrow.setStroke(Color.GREEN);
        root.getChildren().add(arrow);

        root.setOnMouseClicked(evt -> {
            switch (evt.getButton()) {
            case PRIMARY:
                // set pos of end with arrow head
                arrow.setEndX(evt.getX());
                arrow.setEndY(evt.getY());
                break;
            case SECONDARY:
                // set pos of end without arrow head
                arrow.setStartX(evt.getX());
                arrow.setStartY(evt.getY());
                break;
            case MIDDLE:
            case NONE:
            default:
                break;
            }
        });

        Scene scene = new Scene(root, 400, 400);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
