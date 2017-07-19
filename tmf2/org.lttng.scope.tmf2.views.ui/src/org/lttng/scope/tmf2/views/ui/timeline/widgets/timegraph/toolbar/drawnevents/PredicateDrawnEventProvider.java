/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar.drawnevents;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.context.ViewGroupContextManager;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.project.ITraceProject;
import com.efficios.jabberwocky.project.ITraceProjectIterator;
import com.efficios.jabberwocky.timegraph.model.provider.ITimeGraphModelProvider;
import com.efficios.jabberwocky.timegraph.model.provider.drawnevents.TimeGraphDrawnEventProvider;
import com.efficios.jabberwocky.timegraph.model.render.TimeGraphEvent;
import com.efficios.jabberwocky.timegraph.model.render.drawnevents.TimeGraphDrawnEvent;
import com.efficios.jabberwocky.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import com.efficios.jabberwocky.timegraph.model.render.drawnevents.TimeGraphDrawnEventSeries;
import com.efficios.jabberwocky.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.timegraph.model.render.tree.TimeGraphTreeRender;
import com.efficios.jabberwocky.trace.event.ITraceEvent;
import com.google.common.collect.ImmutableList;

class PredicateDrawnEventProvider extends TimeGraphDrawnEventProvider {

    /** Maximum number of matching events */
    private static final int MAX = 2000;

    private final ITimeGraphModelProvider fModelProvider;
    private final Predicate<ITraceEvent> fPredicate;

    public PredicateDrawnEventProvider(TimeGraphDrawnEventSeries drawnEventSeries,
            ITimeGraphModelProvider modelProvider,
            Predicate<ITraceEvent> predicate) {
        super(drawnEventSeries);
        fModelProvider = modelProvider;
        fPredicate = predicate;

        /* Just use whatever trace is currently active */
        traceProjectProperty().bind(ViewGroupContextManager.getCurrent().currentTraceProjectProperty());
    }

    @Override
    public TimeGraphDrawnEventRender getEventRender(TimeGraphTreeRender treeRender,
            TimeRange timeRange, @Nullable FutureTask<?> task) {

        /*
         * FIXME Extremely slow due to iterating from the start of the trace every
         * single time. JW doesn't allow seeking by timestamp directly yet.
         *
         * Final version should keep an iterator at the last selection, so we can
         * continue using it.
         */
        ITraceProject<?, ?> project = traceProjectProperty().get();
        if (project == null) {
            return new TimeGraphDrawnEventRender(timeRange, Collections.EMPTY_LIST);
        }

        int matches = 0;
        List<ITraceEvent> matchingEvents = new LinkedList<>();
        try (ITraceProjectIterator<?> iter = project.iterator()) {
            while (iter.hasNext()) {
                ITraceEvent event = requireNonNull(iter.next());

                // Replace this by an iterator seek once implemented
                if (event.getTimestamp() < timeRange.getStartTime()) {
                    continue;
                }

                if (event.getTimestamp() > timeRange.getEndTime()) {
                    break;
                }

                if (fPredicate.test(event)) {
                    matchingEvents.add(event);
                    if (matches++ > MAX) {
                        break;
                    }
                }
            }

            // Stream version for Java 9:
//            StreamUtils.getStream(iter)
//                .dropWhile(event -> event.getTimestamp() < timeRange.getStartTime())
//                .takeWhile(event -> event.getTimestamp() <= timeRange.getEndTime())
//                .filter(fPredicate)
//                .limit(MAX)
//                .collect(ImmutableList.toImmutableList());
        }

        List<TimeGraphDrawnEvent> drawnEvents = matchingEvents.stream()
                /* trace event -> TimeGraphEvent */
                .map(traceEvent -> {
                    long timestamp = traceEvent.getTimestamp();
                    /*
                     * Find the matching tree element for this trace event, if
                     * there is one.
                     */
                    Optional<TimeGraphTreeElement> treeElem = treeRender.getAllTreeElements().stream()
                            .filter(elem -> {
                                Predicate<ITraceEvent> predicate = elem.getEventMatching();
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
