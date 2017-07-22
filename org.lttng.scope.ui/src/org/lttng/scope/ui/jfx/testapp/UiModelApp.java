package org.lttng.scope.ui.jfx.testapp;

import static java.util.Objects.requireNonNull;

import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.ui.jfx.JfxUtils;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;

public class UiModelApp extends Application {

    /* Value where a raw Pane starts breaking down */
    private static final double PANE_WIDTH = 1000000000.0;
    /* Maximum pane width (roughly a 1-year trace at 0.01 nanos/pixel) */
//    private static final double PANE_WIDTH = 1e16;

    private static final double MAX_WIDTH = 1000000.0;

    private static final double ENTRY_HEIGHT = 20;
    private static final Color BACKGROUD_LINES_COLOR = requireNonNull(Color.LIGHTBLUE);
    private static final String BACKGROUND_STYLE = "-fx-background-color: rgba(255, 255, 255, 255);"; //$NON-NLS-1$

    private static final double SELECTION_STROKE_WIDTH = 1;
    private static final Color SELECTION_STROKE_COLOR = requireNonNull(Color.BLUE);
    private static final Color SELECTION_FILL_COLOR = requireNonNull(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.4));

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(@Nullable Stage primaryStage) throws Exception {
        if (primaryStage == null) {
            return;
        }

        /* Layers */
        Group backgroundLayer = new Group();
        Group statesLayer = new Group();
        Group selectionLayer = new Group();

        /* Top-level */
        Pane contentPane = new Pane(backgroundLayer, statesLayer, selectionLayer);
        contentPane.minWidthProperty().bind(contentPane.prefWidthProperty());
        contentPane.maxWidthProperty().bind(contentPane.prefWidthProperty());
        contentPane.setStyle(BACKGROUND_STYLE);

        ScrollPane scrollPane = new ScrollPane(contentPane);
        scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        BorderPane borderPane = new BorderPane(scrollPane);

        primaryStage.setScene(new Scene(borderPane));
        primaryStage.show();
        primaryStage.setHeight(500);
        primaryStage.setWidth(500);

        contentPane.setPrefHeight(200);
        contentPane.setPrefWidth(PANE_WIDTH);

        drawBackground(backgroundLayer, contentPane);
        drawRectangles(statesLayer);
        drawSelection(selectionLayer, contentPane);
    }

    private static void drawBackground(Group parent, Pane content) {
        DoubleStream.iterate(ENTRY_HEIGHT / 2, i -> i + ENTRY_HEIGHT).limit(10)
                .mapToObj(y -> {
                    Line line = new Line();
                    line.setStartX(0);
                    line.endXProperty().bind(content.widthProperty());
//                    line.setEndX(MAX_WIDTH);
                    line.setStartY(y);
                    line.setEndY(y);

                    line.setStroke(BACKGROUD_LINES_COLOR);
                    line.setStrokeWidth(1.0);
                    return line;
                })
                .forEach(parent.getChildren()::add);
    }

    private static void drawRectangles(Group target) {
        DrawnRectangle rectangle1 = new DrawnRectangle(1, PANE_WIDTH - 20, 2);
        rectangle1.setFill(Color.GREEN);

        DrawnRectangle rectangle2 = new DrawnRectangle(20, 2000, 5);
        rectangle2.setFill(Color.RED);

        DrawnRectangle rectangle3 = new DrawnRectangle(PANE_WIDTH - 2000, PANE_WIDTH - 100, 6);
        rectangle3.setFill(Color.ORANGE);

        target.getChildren().addAll(rectangle1, rectangle2, rectangle3);
    }


    private static class DrawnRectangle extends Rectangle {

        private static final double THICKNESS = 10;

        public DrawnRectangle(double startX, double endX, int entryIndex) {
            double y0 = entryIndex * ENTRY_HEIGHT;
            double yOffset = (ENTRY_HEIGHT - THICKNESS) / 2;

//            double width = endX - startX;
            double width = Math.min(endX - startX, MAX_WIDTH);

            setX(startX);
            setY(y0 + yOffset);
            setWidth(width);
            setHeight(THICKNESS);
        }
    }

    private static void drawSelection(Group parent, Pane content) {
        Rectangle selection1 = new Rectangle();
        Rectangle selection2 = new Rectangle();

        Stream.of(selection1, selection2).forEach(rect -> {
            rect.setMouseTransparent(true);

            rect.setStroke(SELECTION_STROKE_COLOR);
            rect.setStrokeWidth(SELECTION_STROKE_WIDTH);
            rect.setStrokeLineCap(StrokeLineCap.ROUND);
            rect.setFill(SELECTION_FILL_COLOR);

            rect.yProperty().bind(JfxUtils.ZERO_PROPERTY);
            rect.heightProperty().bind(content.heightProperty());
        });

        selection1.setX(200);
        selection1.setWidth(100);

        selection2.setX(PANE_WIDTH - 1000);
        selection2.setWidth(500);

        parent.getChildren().addAll(selection1, selection2);
    }
}
