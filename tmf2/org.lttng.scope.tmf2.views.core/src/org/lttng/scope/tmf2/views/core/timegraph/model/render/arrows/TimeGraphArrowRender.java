/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows;

import java.util.Collection;

import org.lttng.scope.tmf2.views.core.TimeRange;

import com.google.common.collect.ImmutableList;

/**
 * Render of time graph arrows, containing all possible arrows of a single
 * series for a given time range.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphArrowRender {

    private final TimeRange fTimeRange;
    private final Collection<TimeGraphArrow> fArrows;

    public TimeGraphArrowRender(TimeRange range, Collection<TimeGraphArrow> arrows) {
        fTimeRange = range;
        fArrows = ImmutableList.copyOf(arrows);
    }

    public TimeRange getTimeRange() {
        return fTimeRange;
    }

    public Collection<TimeGraphArrow> getArrows() {
        return fArrows;
    }
}
