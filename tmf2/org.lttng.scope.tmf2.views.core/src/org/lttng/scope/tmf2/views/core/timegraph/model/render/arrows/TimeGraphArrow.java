/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows;

import org.lttng.scope.tmf2.views.core.timegraph.model.render.TimeGraphEvent;

public class TimeGraphArrow {

    private final TimeGraphEvent fStartEvent;
    private final TimeGraphEvent fEndEvent;
    private final TimeGraphArrowSeries fArrowSeries;

    public TimeGraphArrow(TimeGraphEvent startEvent, TimeGraphEvent endEvent, TimeGraphArrowSeries series) {
        fStartEvent = startEvent;
        fEndEvent = endEvent;
        fArrowSeries = series;
    }

    public TimeGraphEvent getStartEvent() {
        return fStartEvent;
    }

    public TimeGraphEvent getEndEvent() {
        return fEndEvent;
    }

    public TimeGraphArrowSeries getArrowSeries() {
        return fArrowSeries;
    }

}
