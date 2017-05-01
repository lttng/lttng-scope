/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar.drawnevents;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.context.ViewGroupContext;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.drawnevents.TimeGraphDrawnEventProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.TimeGraphEvent;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEvent;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventSeries;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.google.common.collect.ImmutableList;

class PredicateDrawnEventProvider extends TimeGraphDrawnEventProvider {

    /** Maximum number of matching events */
    private static final int MAX = 2000;

    private final ITimeGraphModelProvider fModelProvider;
    private final Predicate<ITmfEvent> fPredicate;

    public PredicateDrawnEventProvider(TimeGraphDrawnEventSeries drawnEventSeries,
            ITimeGraphModelProvider modelProvider,
            Predicate<ITmfEvent> predicate) {
        super(drawnEventSeries);
        fModelProvider = modelProvider;
        fPredicate = predicate;

        /* Just use whatever trace is currently active */
        traceProperty().bind(ViewGroupContext.getCurrent().currentTraceProperty());
    }

    @Override
    public TimeGraphDrawnEventRender getEventRender(TimeGraphTreeRender treeRender,
            TimeRange timeRange, @Nullable FutureTask<?> task) {

        /* Very TMF-specific */
        ITmfTrace trace = traceProperty().get();
        if (trace == null) {
            return new TimeGraphDrawnEventRender(timeRange, Collections.EMPTY_LIST);
        }

        long startIndex = 0;

        List<ITmfEvent> traceEvents = new LinkedList<>();
        TmfEventRequest req = new TmfEventRequest(ITmfEvent.class,
                timeRange.toTmfTimeRange(),
                startIndex,
                ITmfEventRequest.ALL_DATA,
                ExecutionType.BACKGROUND) {

            private int matches = 0;

            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                if (task != null && task.isCancelled()) {
                    cancel();
                }

                if (fPredicate.test(event)) {
                    matches++;
                    traceEvents.add(event);
                    if (matches > MAX) {
                        done();
                    }
                }
            }
        };
        trace.sendRequest(req);
        try {
            req.waitForCompletion();
        } catch (InterruptedException e) {
        }

        if (req.isCancelled()) {
            return new TimeGraphDrawnEventRender(timeRange, Collections.EMPTY_LIST);
        }

        List<TimeGraphDrawnEvent> drawnEvents = traceEvents.stream()
                /* trace event -> TimeGraphEvent */
                .map(traceEvent -> {
                    long timestamp = traceEvent.getTimestamp().toNanos();
                    /*
                     * Find the matching tree element for this trace event, if
                     * there is one.
                     */
                    Optional<TimeGraphTreeElement> treeElem = treeRender.getAllTreeElements().stream()
                            .filter(elem -> {
                                Predicate<ITmfEvent> predicate = elem.getEventMatching();
                                if (predicate == null) {
                                    return false;
                                }
                                return predicate.test(traceEvent);
                            })
                            .findFirst();

                    if (!treeElem.isPresent()) {
                        return null;
                    }
                    return new TimeGraphEvent(timestamp, treeElem.get());
                })
                .filter(Objects::nonNull)
                /* TimeGraphEvent -> TimeGraphDrawnEvent */
                .map(tgEvent -> new TimeGraphDrawnEvent(tgEvent, getEventSeries(), null))
                .collect(ImmutableList.toImmutableList());

        return new TimeGraphDrawnEventRender(timeRange, drawnEvents);
    }

}
