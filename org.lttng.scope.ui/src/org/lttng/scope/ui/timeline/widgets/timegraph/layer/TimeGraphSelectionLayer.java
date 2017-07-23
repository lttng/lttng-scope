/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.timeline.widgets.timegraph.layer;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.FutureTask;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.ui.jfx.JfxUtils;
import org.lttng.scope.ui.timeline.widgets.timegraph.TimeGraphWidget;
import org.lttng.scope.ui.timeline.widgets.timegraph.VerticalPosition;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 * Sub-control of the time graph widget to handle the selection layer, which
 * displays the current and ongoing selections.
 *
 * It also sends the corresponding selection update whenever a new selection is
 * made.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphSelectionLayer extends TimeGraphLayer {

    /* Style settings. TODO Move to debug options? */
    private static final double SELECTION_STROKE_WIDTH = 1;
    private static final Color SELECTION_STROKE_COLOR = requireNonNull(Color.BLUE);
    private static final Color SELECTION_FILL_COLOR = requireNonNull(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.4));

    /**
     * These events are to be ignored by the time graph pane, they should
     * "bubble up" to the scrollpane to be used for panning.
     */
    private static final Predicate<MouseEvent> MOUSE_EVENT_IGNORED = e -> {
        return (e.getButton() == MouseButton.SECONDARY
                || e.getButton() == MouseButton.MIDDLE
                || e.isControlDown());
    };

    private final Rectangle fSelectionRect = new Rectangle();
    private final Rectangle fOngoingSelectionRect = new Rectangle();

    private final SelectionContext fSelectionCtx = new SelectionContext();

    /**
     * Constructor
     *
     * @param widget
     *            The corresponding time graph widget
     * @param parentGroup
     *            The group to which this layer should add its children
     */
    public TimeGraphSelectionLayer(TimeGraphWidget widget, Group parentGroup) {
        super(widget, parentGroup);

        final Pane timeGraphPane = getWidget().getTimeGraphPane();

        fSelectionRect.setStroke(SELECTION_STROKE_COLOR);
        fSelectionRect.setStrokeWidth(SELECTION_STROKE_WIDTH);
        fSelectionRect.setStrokeLineCap(StrokeLineCap.ROUND);

        Stream.of(fSelectionRect, fOngoingSelectionRect).forEach(rect -> {
            rect.setMouseTransparent(true);
            rect.setFill(SELECTION_FILL_COLOR);

            /*
             * We keep the 'x' property at 0, and we'll use 'layoutX' to set the
             * start position of the rectangles.
             *
             * See https://github.com/lttng/lttng-scope/issues/25
             */
            rect.xProperty().bind(JfxUtils.ZERO_PROPERTY);
            rect.yProperty().bind(JfxUtils.ZERO_PROPERTY);
            rect.heightProperty().bind(timeGraphPane.heightProperty());
        });

        /*
         * Note, unlike most other controls, we will not add/remove children to
         * the target group, we will add them once then toggle their 'visible'
         * property.
         */
        fSelectionRect.setVisible(true);
        fOngoingSelectionRect.setVisible(false);
        getParentGroup().getChildren().addAll(fSelectionRect, fOngoingSelectionRect);

        timeGraphPane.addEventHandler(MouseEvent.MOUSE_PRESSED, fSelectionCtx.fMousePressedEventHandler);
        timeGraphPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, fSelectionCtx.fMouseDraggedEventHandler);
        timeGraphPane.addEventHandler(MouseEvent.MOUSE_RELEASED, fSelectionCtx.fMouseReleasedEventHandler);
    }

    /**
     * Get the rectangle object representing the current selection range.
     *
     * @return The current selection rectangle
     */
    public Rectangle getSelectionRectangle() {
        return fSelectionRect;
    }

    /**
     * Get the rectangle object representing the ongoing selection. It is
     * displayed while the user holds the mouse down and drags to make a
     * selection, but before the mouse is released. The "real" selection is only
     * applied on mouse release.
     *
     * @return The ongoing selection rectangle
     */
    public Rectangle getOngoingSelectionRectangle() {
        return fOngoingSelectionRect;
    }

    @Override
    public void drawContents(TimeGraphTreeRender treeRender, TimeRange timeRange,
            VerticalPosition vPos, @Nullable FutureTask<?> task) {
        drawSelection(timeRange);
    }

    @Override
    public void clear() {
        /* We don't have to clear anything */
    }

    /**
     * Draw a new "current" selection. For times where the selection is updated
     * elsewhere in the framework.
     *
     * @param timeRange
     *            The time range of the new selection
     */
    public void drawSelection(TimeRange timeRange) {
        double xStart = getWidget().timestampToPaneXPos(timeRange.getStartTime());
        double xEnd = getWidget().timestampToPaneXPos(timeRange.getEndTime());
        double xWidth = xEnd - xStart;

        fSelectionRect.setLayoutX(xStart);
        fSelectionRect.setWidth(xWidth);

        fSelectionRect.setVisible(true);
    }

    /**
     * Class encapsulating the time range selection, related drawing and
     * listeners.
     */
    private class SelectionContext {

        private boolean fOngoingSelection;
        private double fMouseOriginX;

        public final EventHandler<MouseEvent> fMousePressedEventHandler = e -> {
            if (MOUSE_EVENT_IGNORED.test(e)) {
                return;
            }
            e.consume();

            if (fOngoingSelection) {
                return;
            }

            /* Remove the current selection, if there is one */
            fSelectionRect.setVisible(false);

            fMouseOriginX = e.getX();

            fOngoingSelectionRect.setLayoutX(fMouseOriginX);
            fOngoingSelectionRect.setWidth(0);
            fOngoingSelectionRect.setVisible(true);

            fOngoingSelection = true;
        };

        public final EventHandler<MouseEvent> fMouseDraggedEventHandler = e -> {
            if (MOUSE_EVENT_IGNORED.test(e)) {
                return;
            }
            e.consume();

            double newX = e.getX();
            double offsetX = newX - fMouseOriginX;

            if (offsetX > 0) {
                fOngoingSelectionRect.setLayoutX(fMouseOriginX);
                fOngoingSelectionRect.setWidth(offsetX);
            } else {
                fOngoingSelectionRect.setLayoutX(newX);
                fOngoingSelectionRect.setWidth(-offsetX);
            }

        };

        public final EventHandler<MouseEvent> fMouseReleasedEventHandler = e -> {
            if (MOUSE_EVENT_IGNORED.test(e)) {
                return;
            }
            e.consume();

            fOngoingSelectionRect.setVisible(false);

            /* Send a time range selection signal for the currently selected time range */
            double startX = Math.max(0, fOngoingSelectionRect.getLayoutX());
            // FIXME Possible glitch when selecting backwards outside of the window
            double endX = Math.min(getWidget().getTimeGraphPane().getWidth(), startX + fOngoingSelectionRect.getWidth());
            long tsStart = getWidget().paneXPosToTimestamp(startX);
            long tsEnd = getWidget().paneXPosToTimestamp(endX);

            getWidget().getControl().updateTimeRangeSelection(TimeRange.of(tsStart, tsEnd));

            fOngoingSelection = false;
        };
    }

}
