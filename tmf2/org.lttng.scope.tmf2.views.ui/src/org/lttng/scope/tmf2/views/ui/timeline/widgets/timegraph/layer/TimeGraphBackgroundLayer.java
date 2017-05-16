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
import java.util.concurrent.FutureTask;
import java.util.stream.DoubleStream;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;
import org.lttng.scope.tmf2.views.ui.jfx.JfxUtils;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.VerticalPosition;

import javafx.application.Platform;
import javafx.scene.shape.Line;

/**
 * Sub-control of the time graph widget taking care of simply drawing the
 * background lines in the area of the current visible window.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphBackgroundLayer extends TimeGraphLayer {

    /**
     * Constructor
     *
     * @param widget
     *            Main widget which this control supports
     */
    public TimeGraphBackgroundLayer(TimeGraphWidget widget) {
        super(widget);
    }

    @Override
    public void drawContents(TimeGraphTreeRender treeRender, TimeRange timeRange,
            VerticalPosition vPos, @Nullable FutureTask<?> task) {
        final double entryHeight = TimeGraphWidget.ENTRY_HEIGHT;

        final int entriesToPrefetch = getWidget().getDebugOptions().entryPadding.get();
        int totalNbEntries = treeRender.getAllTreeElements().size();

        final double timeGraphWidth = getWidget().getTimeGraphPane().getWidth();
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
            getChildren().clear();
            getChildren().addAll(lines);
        });
    }

    @Override
    public void clear() {
        JfxUtils.runOnMainThread(() -> getChildren().clear());
    }

}
