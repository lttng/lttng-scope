/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.render.arrows;

import java.util.Collection;
import java.util.Collections;

import com.efficios.jabberwocky.common.TimeRange;
import com.google.common.collect.ImmutableList;

/**
 * Render of time graph arrows, containing all possible arrows of a single
 * series for a given time range.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphArrowRender {

    /** Empty arrow render, can be used instead of a null value */
    public static final TimeGraphArrowRender EMPTY_RENDER =
            new TimeGraphArrowRender(TimeRange.of(0, 0), Collections.EMPTY_LIST);

    private final TimeRange fTimeRange;
    private final Collection<TimeGraphArrow> fArrows;

    /**
     * Constructor
     *
     * @param range
     *            Time range of this arrow render. For reference only, should
     *            probably match the time range of the query that created this
     *            render.
     * @param arrows
     *            The arrows contained in this render.
     */
    public TimeGraphArrowRender(TimeRange range, Iterable<TimeGraphArrow> arrows) {
        fTimeRange = range;
        fArrows = ImmutableList.copyOf(arrows);
    }

    /**
     * Get the time range of this arrow render.
     *
     * @return The time range
     */
    public TimeRange getTimeRange() {
        return fTimeRange;
    }

    /**
     * Get the arrows contained in this render.
     *
     * @return The arrows
     */
    public Collection<TimeGraphArrow> getArrows() {
        return fArrows;
    }

}
