/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem;

import java.util.List;
import java.util.concurrent.FutureTask;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.arrows.TimeGraphModelArrowProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.TimeGraphEvent;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrow;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowSeries;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.google.common.collect.ImmutableList;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;

public class StateSystemModelArrowProvider extends TimeGraphModelArrowProvider {

    private final String fStateSystemModuleId;

    private transient @Nullable ITmfStateSystem fStateSystem = null;

    public StateSystemModelArrowProvider(TimeGraphArrowSeries arrowSeries,
            String stateSystemModuleId) {
        super(arrowSeries);
        fStateSystemModuleId = stateSystemModuleId;

        /*
         * Change listener which will take care of keeping the target state
         * system up to date.
         */
        traceProperty().addListener((obs, oldValue, newValue) -> {
            ITmfTrace trace = newValue;
            if (trace == null) {
                fStateSystem = null;
                return;
            }
            fStateSystem = TmfStateSystemAnalysisModule.getStateSystem(trace, fStateSystemModuleId);
        });
    }

    @Override
    public TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender, TimeRange timeRange, @Nullable FutureTask<?> task) {
        ITmfStateSystem ss = fStateSystem;
        if (ss == null) {
            return TimeGraphArrowRender.EMPTY_RENDER;
        }

        // TODO temp test code
        TimeGraphArrowSeries series = getArrowSeries();
        List<TimeGraphTreeElement> treeElems = treeRender.getAllTreeElements();

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
