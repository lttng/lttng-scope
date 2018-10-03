/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.views.timegraph.model.render.drawnevents;

import java.util.Collection;

import com.efficios.jabberwocky.common.TimeRange;
import com.google.common.collect.ImmutableList;

/**
 * Render of time graph drawn events. It can cover several (usually all) tree
 * elements, unlike the TimeGraphStateRender which covers only a single element.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphDrawnEventRender {

    private final TimeRange fTimeRange;
    private final Collection<TimeGraphDrawnEvent> fEvents;

    /**
     * Constructor
     *
     * @param timeRange
     *            The time range of this draw event render. Should usually match
     *            the time range of the query that created this render.
     * @param events
     *            The drawn events contained in this render
     */
    public TimeGraphDrawnEventRender(TimeRange timeRange,
            Iterable<TimeGraphDrawnEvent> events) {

        fTimeRange = timeRange;
        fEvents = ImmutableList.copyOf(events);
    }

    /**
     * Get the time range of this render
     *
     * @return The time range
     */
    public TimeRange getTimeRange() {
        return fTimeRange;
    }

    /**
     * Get the drawn events that are part of this render.
     *
     * @return The drawn events
     */
    public Collection<TimeGraphDrawnEvent> getEvents() {
        return fEvents;
    }
}
