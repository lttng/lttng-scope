package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.examples;

import org.eclipse.jdt.annotation.NonNullByDefault;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

@NonNullByDefault({})
public class Example2 extends Application {

    private static final double TEN_BILLIONS = 10000000000.0;

    @Override
    public void start(Stage stage) {
//        Node content = new Rectangle(1000, 700, Color.GREEN);
        Pane pane = new Pane();
        pane.setPrefSize(TEN_BILLIONS, TEN_BILLIONS);
        ScrollPane scrollPane = new ScrollPane(pane);
        scrollPane.setPrefSize(500, 300);

        ChangeListener<Object> changeListener = new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
                System.out.println("source=" + observable.toString());

                double hmin = scrollPane.getHmin();
                double hmax = scrollPane.getHmax();
                double hvalue = scrollPane.getHvalue();
                double contentWidth = pane.getLayoutBounds().getWidth();
                double viewportWidth = scrollPane.getViewportBounds().getWidth();

                double hoffset =
                    Math.max(0, contentWidth - viewportWidth) * (hvalue - hmin) / (hmax - hmin);

                double vmin = scrollPane.getVmin();
                double vmax = scrollPane.getVmax();
                double vvalue = scrollPane.getVvalue();
                double contentHeight = pane.getLayoutBounds().getHeight();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();

                double voffset =
                    Math.max(0,  contentHeight - viewportHeight) * (vvalue - vmin) / (vmax - vmin);

                System.out.printf("Offset: [%.1f, %.1f] width: %.1f height: %.1f %n",
                        hoffset, voffset, viewportWidth, viewportHeight);
            }
        };
        scrollPane.viewportBoundsProperty().addListener(changeListener);
        scrollPane.hvalueProperty().addListener(changeListener);
        scrollPane.vvalueProperty().addListener(changeListener);

        /* Drawing on the region */
        Canvas canvas1 = new Canvas(100, 100);
        canvas1.relocate(TEN_BILLIONS - 100, 0);
        canvas1.getGraphicsContext2D().strokeOval(60, 60, 30, 30);

        Canvas canvas2 = new Canvas(100, 100);
        canvas2.relocate(TEN_BILLIONS - 100, TEN_BILLIONS - 100);
        canvas2.getGraphicsContext2D().fillOval(60, 60, 30, 30);

        pane.getChildren().addAll(canvas1, canvas2);

        /* Showing the scene */
        Scene scene = new Scene(scrollPane, 640, 480);
        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}