package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.examples;

import java.net.MalformedURLException;

import org.eclipse.jdt.annotation.NonNullByDefault;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

@NonNullByDefault({})
public class ZoomExample extends Application {

    public static Region createContent() {
        double width = 1000;
        double height = 1000;

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.LIGHTGREY);
        gc.fillRect(0, 0, width, height);

        gc.setStroke(Color.BLUE);
        gc.beginPath();

        for (int i = 50; i < width; i += 50) {
            gc.moveTo(i, 0);
            gc.lineTo(i, height);
        }

        for (int i = 50; i < height; i += 50) {
            gc.moveTo(0, i);
            gc.lineTo(width, i);
        }
        gc.stroke();

        Pane content = new Pane(
                new Circle(50, 50, 20),
                new Circle(120, 90, 20, Color.RED),
                new Circle(200, 70, 20, Color.GREEN)
        );

        StackPane result = new StackPane(canvas, content);
        result.setAlignment(Pos.TOP_LEFT);

        class DragData {

            double startX;
            double startY;
            double startLayoutX;
            double startLayoutY;
            Node dragTarget;
        }

        DragData dragData = new DragData();

        content.setOnMousePressed(evt -> {
            if (evt.getTarget() != content) {
                // initiate drag gesture, if a child of content receives the
                // event to prevent ScrollPane from panning.
                evt.consume();
                evt.setDragDetect(true);
            }
        });

        content.setOnDragDetected(evt -> {
            Node n = (Node) evt.getTarget();
            if (n != content) {
                // set start paremeters
                while (n.getParent() != content) {
                    n = n.getParent();
                }
                dragData.startX = evt.getX();
                dragData.startY = evt.getY();
                dragData.startLayoutX = n.getLayoutX();
                dragData.startLayoutY = n.getLayoutY();
                dragData.dragTarget = n;
                n.startFullDrag();
                evt.consume();
            }
        });

        // stop dragging when mouse is released
        content.setOnMouseReleased(evt -> dragData.dragTarget = null);

        content.setOnMouseDragged(evt -> {
            if (dragData.dragTarget != null) {
                // move dragged node
                dragData.dragTarget.setLayoutX(evt.getX() + dragData.startLayoutX - dragData.startX);
                dragData.dragTarget.setLayoutY(evt.getY() + dragData.startLayoutY - dragData.startY);
//                Point2D p = new Point2D(evt.getX(), evt.getY());
                evt.consume();
            }
        });

        return result;
    }

    @Override
    public void start(Stage primaryStage) throws MalformedURLException {
        Region zoomTarget = createContent();
        zoomTarget.setPrefSize(1000, 1000);
        zoomTarget.setOnDragDetected(evt -> {
            Node target = (Node) evt.getTarget();
            while (target != zoomTarget && target != null) {
                target = target.getParent();
            }
            if (target != null) {
                target.startFullDrag();
            }
        });

        Group group = new Group(zoomTarget);

        // stackpane for centering the content, in case the ScrollPane viewport
        // is larget than zoomTarget
        StackPane content = new StackPane(group);
        group.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
            // keep it at least as large as the content
            content.setMinWidth(newBounds.getWidth());
            content.setMinHeight(newBounds.getHeight());
        });

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPannable(true);
        scrollPane.viewportBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
            // use vieport size, if not too small for zoomTarget
            content.setPrefSize(newBounds.getWidth(), newBounds.getHeight());
        });

        content.setOnScroll(evt -> {
            if (evt.isControlDown()) {
                evt.consume();

                final double zoomFactor = evt.getDeltaY() > 0 ? 1.2 : 1 / 1.2;

                Bounds groupBounds = group.getLayoutBounds();
                final Bounds viewportBounds = scrollPane.getViewportBounds();

                // calculate pixel offsets from [0, 1] range
                double valX = scrollPane.getHvalue() * (groupBounds.getWidth() - viewportBounds.getWidth());
                double valY = scrollPane.getVvalue() * (groupBounds.getHeight() - viewportBounds.getHeight());

                // convert content coordinates to zoomTarget coordinates
                Point2D posInZoomTarget = zoomTarget.parentToLocal(group.parentToLocal(new Point2D(evt.getX(), evt.getY())));

                // calculate adjustment of scroll position (pixels)
                Point2D adjustment = zoomTarget.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

                // do the resizing
                zoomTarget.setScaleX(zoomFactor * zoomTarget.getScaleX());
                zoomTarget.setScaleY(zoomFactor * zoomTarget.getScaleY());

                // refresh ScrollPane scroll positions & content bounds
                scrollPane.layout();

                // convert back to [0, 1] range
                // (too large/small values are automatically corrected by ScrollPane)
                groupBounds = group.getLayoutBounds();
                scrollPane.setHvalue((valX + adjustment.getX()) / (groupBounds.getWidth() - viewportBounds.getWidth()));
                scrollPane.setVvalue((valY + adjustment.getY()) / (groupBounds.getHeight() - viewportBounds.getHeight()));
            }
        });

        StackPane left = new StackPane(new Label("Left Menu"));
        SplitPane root = new SplitPane(left, scrollPane);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}