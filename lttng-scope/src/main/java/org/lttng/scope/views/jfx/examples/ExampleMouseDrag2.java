package org.lttng.scope.views.jfx.examples;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ExampleMouseDrag2 extends Application {

//    public static Image image = new Image("http://upload.wikimedia.org/wikipedia/commons/thumb/4/41/Siberischer_tiger_de_edit02.jpg/320px-Siberischer_tiger_de_edit02.jpg");
    public Image image = new Image("file:///home/alexandre/Desktop/tiger.jpg");
//    public Image image = new Image( getClass().getResource( "/home/alexandre/Desktop/tiger.jpg").toExternalForm());

    SelectionModel selectionModel = new SelectionModel();

    DragMouseGestures dragMouseGestures = new DragMouseGestures();

    static Random rnd = new Random();

    @Override
    public void start(Stage primaryStage) {
        if (primaryStage == null) {
            return;
        }

        Pane pane = new Pane();
        pane.setStyle("-fx-background-color:white");

        new RubberBandSelection( pane);

        double width = 200;
        double height = 160;

        double padding = 20;
        for( int row=0; row < 4; row++) {
            for( int col=0; col < 4; col++) {

                Selectable selectable = new Selectable( width, height);
                selectable.relocate( padding * (col+1) + width * col, padding * (row + 1) + height * row);

                pane.getChildren().add(selectable);

                dragMouseGestures.makeDraggable(selectable);

            }
        }

        Label infoLabel = new Label( "Drag on scene for Rubberband Selection. Shift+Click to add to selection, CTRL+Click to toggle selection. Drag selected nodes for multi-dragging.");
        pane.getChildren().add( infoLabel);

        Scene scene = new Scene( pane, 1600, 900);
        scene.getStylesheets().add( getClass().getResource("application.css").toExternalForm());

        primaryStage.setScene( scene);
        primaryStage.show();



    }

    private class Selectable extends Region {

        ImageView view;

        public Selectable( double width, double height) {

            view = new ImageView( image);
            view.setFitWidth(width);
            view.setFitHeight(height);

            getChildren().add( view);

            this.setPrefSize(width, height);
        }

    }

    private class SelectionModel {

        Set<Node> selection = new HashSet<>();

        public void add( Node node) {

            if( !node.getStyleClass().contains("highlight")) {
                node.getStyleClass().add( "highlight");
            }

            selection.add( node);
        }

        public void remove( Node node) {
            node.getStyleClass().remove( "highlight");
            selection.remove( node);
        }

        public void clear() {

            while( !selection.isEmpty()) {
                remove( selection.iterator().next());
            }

        }

        public boolean contains( Node node) {
            return selection.contains(node);
        }

//        public int size() {
//            return selection.size();
//        }

        public void log() {
            System.out.println( "Items in model: " + Arrays.asList( selection.toArray()));
        }

    }

    private class DragMouseGestures {

        final DragContext dragContext = new DragContext();

        private boolean enabled = false;

        public void makeDraggable(final Node node) {

            node.setOnMousePressed(onMousePressedEventHandler);
            node.setOnMouseDragged(onMouseDraggedEventHandler);
            node.setOnMouseReleased(onMouseReleasedEventHandler);

        }

        EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                // don't do anything if the user is in the process of adding to the selection model
                if( event.isControlDown() || event.isShiftDown()) {
                    return;
                }

                Node node = (Node) event.getSource();

                dragContext.x = node.getTranslateX() - event.getSceneX();
                dragContext.y = node.getTranslateY() - event.getSceneY();

                // clear the model if the current node isn't in the selection => new selection
                if( !selectionModel.contains(node)) {
                    selectionModel.clear();
                    selectionModel.add( node);
                }

                // flag that the mouse released handler should consume the event, so it won't bubble up to the pane which has a rubberband selection mouse released handler
                enabled = true;

                // prevent rubberband selection handler
                event.consume();
            }
        };

        EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if( !enabled) {
                    return;
                }

                // all in selection
                for( Node node: selectionModel.selection) {
                    node.setTranslateX( dragContext.x + event.getSceneX());
                    node.setTranslateY( dragContext.y + event.getSceneY());
                }

            }
        };

        EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                // prevent rubberband selection handler
                if( enabled) {

                    // set node's layout position to current position,remove translate coordinates
                    for( Node node: selectionModel.selection) {
                        fixPosition(node);
                    }

                    enabled = false;

                    event.consume();
                }
            }
        };

        /**
         * Set node's layout position to current position, remove translate coordinates.
         * @param node
         */
        private void fixPosition( Node node) {

            double x = node.getTranslateX();
            double y = node.getTranslateY();

            node.relocate(node.getLayoutX() + x, node.getLayoutY() + y);

            node.setTranslateX(0);
            node.setTranslateY(0);

        }

        class DragContext {

            double x;
            double y;

        }

    }

    private class RubberBandSelection {

        final DragContext dragContext = new DragContext();
        final Rectangle rect;

        Pane group;
        boolean enabled = false;

        public RubberBandSelection( Pane group) {

            this.group = group;

            rect = new Rectangle( 0,0,0,0);
            rect.setStroke(Color.BLUE);
            rect.setStrokeWidth(1);
            rect.setStrokeLineCap(StrokeLineCap.ROUND);
            rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

            group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

        }

        EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                // simple flag to prevent multiple handling of this event or we'd get an exception because rect is already on the scene
                // eg if you drag with left mouse button and while doing that click the right mouse button
                if( enabled) {
                    return;
                }

                dragContext.mouseAnchorX = event.getSceneX();
                dragContext.mouseAnchorY = event.getSceneY();

                rect.setX(dragContext.mouseAnchorX);
                rect.setY(dragContext.mouseAnchorY);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().add( rect);

                event.consume();

                enabled = true;
            }
        };

        EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if( !event.isShiftDown() && !event.isControlDown()) {
                    selectionModel.clear();
                }

                for( Node node: group.getChildren()) {

                    if( node instanceof Selectable) {
                        if( node.getBoundsInParent().intersects( rect.getBoundsInParent())) {

                            if( event.isShiftDown()) {

                                selectionModel.add( node);

                            } else if( event.isControlDown()) {

                                if( selectionModel.contains( node)) {
                                    selectionModel.remove( node);
                                } else {
                                    selectionModel.add( node);
                                }
                            } else {
                                selectionModel.add( node);
                            }

                        }
                    }

                }

                selectionModel.log();

                rect.setX(0);
                rect.setY(0);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().remove( rect);

                event.consume();

                enabled = false;
            }
        };

        EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                double offsetX = event.getSceneX() - dragContext.mouseAnchorX;
                double offsetY = event.getSceneY() - dragContext.mouseAnchorY;

                if( offsetX > 0) {
                    rect.setWidth( offsetX);
                } else {
                    rect.setX(event.getSceneX());
                    rect.setWidth(dragContext.mouseAnchorX - rect.getX());
                }

                if( offsetY > 0) {
                    rect.setHeight( offsetY);
                } else {
                    rect.setY(event.getSceneY());
                    rect.setHeight(dragContext.mouseAnchorY - rect.getY());
                }

                event.consume();

            }
        };

        private final class DragContext {

            public double mouseAnchorX;
            public double mouseAnchorY;


        }
    }


    public static void main(String[] args) {
        launch(args);
    }

}
