/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.layer;

import java.util.LinkedList;
import java.util.stream.DoubleStream;

import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.ui.jfx.JfxUtils;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.VerticalPosition;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.shape.Line;

/**
 * Sub-control of the time graph widget taking care of simply drawing the
 * background lines in the area of the current visible window.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphBackgroundLayer {

    private final TimeGraphWidget fWidget;
    private final Group fPaintTarget;

    /**
     * Constructor
     *
     * @param widget
     *            Main widget which this control supports
     * @param paintTarget
     *            The Group to which the scenegraph objects should be added
     */
    public TimeGraphBackgroundLayer(TimeGraphWidget widget, Group paintTarget) {
        fWidget = widget;
        fPaintTarget = paintTarget;
    }

    /**
     * Paint the contents
     *
     * @param horizontalRange
     *            The horizontal "position" of the visible window
     * @param vPos
     *            The vertical position of the visible window
     */
    public void paintBackground(TimeRange horizontalRange, VerticalPosition vPos) {
        final double entryHeight = TimeGraphWidget.ENTRY_HEIGHT;

        final int entriesToPrefetch = fWidget.getDebugOptions().entryPadding.get();
        int totalNbEntries = fWidget.getLatestTreeRender().getAllTreeElements().size();

        final double timeGraphWidth = fWidget.getTimeGraphPane().getWidth();
        final double paintTopPos = Math.max(0.0, vPos.fTopPos - entriesToPrefetch * entryHeight);
        final double paintBottomPos = Math.min(vPos.fBottomPos + entriesToPrefetch * entryHeight,
                /*
                 * If there are less tree elements than can fill the window,
                 * stop at the end of the real tree elements.
                 */
                totalNbEntries * entryHeight);

        LinkedList<Line> lines = new LinkedList<>();
        DoubleStream.iterate((entryHeight / 2), y -> y + entryHeight)
                // TODO Java 9 will allow using dropWhile()/takeWhile()/collect
                .filter(y -> y > paintTopPos)
                .peek(y -> {
                    Line line = new Line(0, y, timeGraphWidth, y);
                    line.setStroke(TimeGraphWidget.BACKGROUD_LINES_COLOR);
                    line.setStrokeWidth(1.0);

                    lines.add(line);
                })
                .allMatch(y -> y < paintBottomPos);
        // The list contains the first element that didn't match the predicate,
        // we don't want it.
        if (!lines.isEmpty()) {
            lines.removeLast();
        }

        Platform.runLater(() -> {
            fPaintTarget.getChildren().clear();
            fPaintTarget.getChildren().addAll(lines);
        });
    }

    /**
     * Clear the scenegraph objects created by this control.
     */
    public void clear() {
        JfxUtils.runOnMainThread(() -> fPaintTarget.getChildren().clear());
    }
}
