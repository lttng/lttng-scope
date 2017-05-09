/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.jfx.testapp;

import java.util.List;
import java.util.concurrent.FutureTask;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.arrows.TimeGraphModelArrowProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.TimeGraphEvent;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrow;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowSeries;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowSeries.LineStyle;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.google.common.collect.ImmutableList;

class TestModelArrowProvider1 extends TimeGraphModelArrowProvider {

    public static final String SERIES_NAME = "Red";

    private static final TimeGraphArrowSeries ARROW_SERIES = new TimeGraphArrowSeries(
            SERIES_NAME,
            new ColorDefinition(255, 0, 0),
            LineStyle.FULL);

    public TestModelArrowProvider1() {
        super(ARROW_SERIES);
    }

    @Override
    public TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender, TimeRange timeRange, @Nullable FutureTask<?> task) {
        TimeGraphArrowSeries series = getArrowSeries();
        List<TimeGraphTreeElement> treeElems = treeRender.getAllTreeElements();

        /* Draw 3 arrows total */
        TimeGraphEvent startEvent = new TimeGraphEvent(ts(timeRange, 0.1), treeElems.get(0));
        TimeGraphEvent endEvent = new TimeGraphEvent(ts(timeRange, 0.3), treeElems.get(5));
        TimeGraphArrow arrow1 = new TimeGraphArrow(startEvent, endEvent, series);

        startEvent = new TimeGraphEvent(ts(timeRange, 0.2), treeElems.get(3));
        endEvent = new TimeGraphEvent(ts(timeRange, 0.5), treeElems.get(12));
        TimeGraphArrow arrow2 = new TimeGraphArrow(startEvent, endEvent, series);

        startEvent = new TimeGraphEvent(ts(timeRange, 0.6), treeElems.get(15));
        endEvent = new TimeGraphEvent(ts(timeRange, 0.8), treeElems.get(2));
        TimeGraphArrow arrow3 = new TimeGraphArrow(startEvent, endEvent, series);

        List<TimeGraphArrow> arrows = ImmutableList.of(arrow1, arrow2, arrow3);
        return new TimeGraphArrowRender(timeRange, arrows);
    }

    private static long ts(TimeRange range, double ratio) {
        return (long) (range.getDuration() * ratio + range.getStart());
    }

}
