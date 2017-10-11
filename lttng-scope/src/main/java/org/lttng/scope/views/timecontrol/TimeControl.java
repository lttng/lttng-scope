/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timecontrol;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.context.ViewGroupContext;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.lttng.scope.views.context.ViewGroupContextManager;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.lttng.scope.views.timecontrol.TimestampConversion.tsToString;

public class TimeControl extends BorderPane {

    /* UI Text */
    private static final String LABEL_VISIBLE_TIME_RANGE = "Visible Time Range";
    private static final String LABEL_SELECTION_TIME_RANGE = "Selection Time Range";
    private static final String LABEL_PROJECT_RANGE = "Trace Project Time Range";
    private static final String LABEL_START = "Start";
    private static final String LABEL_END = "End";
    private static final String LABEL_SPAN = "Span (s)";



    private static final long MINIMUM_VISIBLE_RANGE = 10000;
    private static final Font TITLE_FONT = Font.font(null, FontWeight.BOLD, -1);
    private static final Insets GRID_PADDING = new Insets(10);

    private final ObjectProperty<ViewGroupContext> viewContextProperty = new SimpleObjectProperty<>(ViewGroupContextManager.getCurrent());

    private final TextField[] textFields;

    public TimeControl() {
        ViewGroupContext viewCtx = viewContextProperty.get();
        // TODO Handle case where this view's ViewContext can change

        TimeRangeTextFields visibleRangeFields = new TimeRangeTextFields(viewCtx.getCurrentProjectFullRange(), MINIMUM_VISIBLE_RANGE);
        TimeRangeTextFields selectionRangeFields = new TimeRangeTextFields(viewCtx.getCurrentProjectFullRange(), null);

        textFields = new TextField[] {
                visibleRangeFields.getStartTextField(),
                visibleRangeFields.getEndTextField(),
                visibleRangeFields.getDurationTextField(),
                selectionRangeFields.getStartTextField(),
                selectionRangeFields.getEndTextField(),
                selectionRangeFields.getDurationTextField()
        };
        Arrays.stream(textFields).forEach(tf -> {
            tf.setAlignment(Pos.CENTER_LEFT);
        });

        TextField[] projRangeTextFields = Stream.generate(() -> new TextField()).limit(3)
                .peek(tf -> {
                    tf.setEditable(false);
                    tf.setPrefWidth(220);
                    tf.setAlignment(Pos.CENTER_LEFT);
                    tf.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(2), null)));
                })
                .toArray(TextField[]::new);

        Label[] topHeaderLabels = Stream.of(LABEL_START, LABEL_END, LABEL_SPAN)
                .map(text -> new Label(text))
                .peek(label -> {
                    label.setFont(TITLE_FONT);
                    GridPane.setHalignment(label, HPos.CENTER);
                })
                .toArray(Label[]::new);

        Label[] leftHeaderLabels = Stream.of(LABEL_VISIBLE_TIME_RANGE, LABEL_SELECTION_TIME_RANGE, LABEL_PROJECT_RANGE)
                .map(text -> new Label(text))
                .peek(label -> {
                    label.setFont(TITLE_FONT);
                    GridPane.setHalignment(label, HPos.RIGHT);
                    GridPane.setValignment(label, VPos.CENTER);
                })
                .toArray(Label[]::new);

        /* Setup the elements in the grid */
        GridPane grid = new GridPane();
        grid.setPadding(GRID_PADDING);
        grid.setHgap(2);
        grid.setVgap(2);

        grid.add(topHeaderLabels[0], 1, 0);
        grid.add(topHeaderLabels[1], 2, 0);
        grid.add(topHeaderLabels[2], 3, 0);

        grid.add(leftHeaderLabels[0], 0, 1);
        grid.add(textFields[0], 1, 1);
        grid.add(textFields[1], 2, 1);
        grid.add(textFields[2], 3, 1);

        grid.add(leftHeaderLabels[1], 0, 2);
        grid.add(textFields[3], 1, 2);
        grid.add(textFields[4], 2, 2);
        grid.add(textFields[5], 3, 2);

        grid.add(leftHeaderLabels[2], 0, 3);
        grid.add(projRangeTextFields[0], 1, 3);
        grid.add(projRangeTextFields[1], 2, 3);
        grid.add(projRangeTextFields[2], 3, 3);


        viewCtx.currentTraceProjectProperty().addListener((obs, oldVal, newVal) -> {
            /* Update the displayed trace's time range. */
            TimeRange projRange;
            if (newVal == null) {
               projRange = ViewGroupContext.UNINITIALIZED_RANGE;
            } else {
                projRange  = newVal.getFullRange();
            }
            projRangeTextFields[0].setText(tsToString(projRange.getStartTime()));
            projRangeTextFields[1].setText(tsToString(projRange.getEndTime()));
            projRangeTextFields[2].setText(tsToString(projRange.getDuration()));

            /* Update the text fields' limits */
            visibleRangeFields.setLimits(projRange);
            selectionRangeFields.setLimits(projRange);

            /* Read initial dynamic range values from the view context. */
            TimeRange visibleRange = viewCtx.getCurrentVisibleTimeRange();
            TimeRange selectionRange = viewCtx.getCurrentSelectionTimeRange();
            textFields[0].setText(tsToString(visibleRange.getStartTime()));
            textFields[1].setText(tsToString(visibleRange.getEndTime()));
            textFields[2].setText(tsToString(visibleRange.getDuration()));
            textFields[3].setText(tsToString(selectionRange.getStartTime()));
            textFields[4].setText(tsToString(selectionRange.getEndTime()));
            textFields[5].setText(tsToString(selectionRange.getDuration()));

            attachRangeListeners(viewCtx);

            /* Bind underlying properties */
            visibleRangeFields.timeRangeProperty().bindBidirectional(viewCtx.currentVisibleTimeRangeProperty());
            selectionRangeFields.timeRangeProperty().bindBidirectional(viewCtx.currentSelectionTimeRangeProperty());
        });

        setCenter(grid);
    }

    /**
     * Add listeners to update the values when the context's time ranges change.
     * Note we dot no use binds here, we do not want every single keystroke in the
     * text fields to update the view context values!
     */
    private void attachRangeListeners(ViewGroupContext ctx) {
        ctx.currentVisibleTimeRangeProperty().addListener((obs, oldVal, newVal) -> {
            textFields[0].setText(tsToString(newVal.getStartTime()));
            textFields[1].setText(tsToString(newVal.getEndTime()));
            textFields[2].setText(tsToString(newVal.getDuration()));
        });
        ctx.currentSelectionTimeRangeProperty().addListener((obs, oldVal, newVal) -> {
            textFields[3].setText(tsToString(newVal.getStartTime()));
            textFields[4].setText(tsToString(newVal.getEndTime()));
            textFields[5].setText(tsToString(newVal.getDuration()));
        });
    }

}
