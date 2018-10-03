/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.render.arrows;

import com.efficios.jabberwocky.views.timegraph.model.render.TimeGraphEvent;

/**
 * Model representation of a timegraph arrow.
 *
 * An arrow links two {@link TimeGraphEvent}s, and has a direction (a start
 * event and a end event). The two events can belong to different tree elements,
 * or the same one.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphArrow {

    private final TimeGraphEvent fStartEvent;
    private final TimeGraphEvent fEndEvent;
    private final TimeGraphArrowSeries fArrowSeries;

    /**
     * Constructor
     *
     * @param startEvent
     *            The start event of this arrow
     * @param endEvent
     *            The end event of this arrow. The drawn arrowhead should be on
     *            this side
     * @param series
     *            The series to which the arrow belongs. This series contains
     *            all the styling information.
     */
    public TimeGraphArrow(TimeGraphEvent startEvent, TimeGraphEvent endEvent, TimeGraphArrowSeries series) {
        fStartEvent = startEvent;
        fEndEvent = endEvent;
        fArrowSeries = series;
    }

    /**
     * Get the start event of this arrow.
     *
     * @return The start event
     */
    public TimeGraphEvent getStartEvent() {
        return fStartEvent;
    }

    /**
     * Get the end event of this arrow.
     *
     * @return The end event
     */
    public TimeGraphEvent getEndEvent() {
        return fEndEvent;
    }

    /**
     * Get the series of this arrow.
     *
     * @return The arrow series
     */
    public TimeGraphArrowSeries getArrowSeries() {
        return fArrowSeries;
    }

}
