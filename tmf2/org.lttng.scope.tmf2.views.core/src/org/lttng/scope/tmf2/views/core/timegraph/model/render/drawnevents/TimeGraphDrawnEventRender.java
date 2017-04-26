/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents;

import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;

import com.google.common.collect.ImmutableList;

public class TimeGraphDrawnEventRender {

    private final TimeRange fTimeRange;
    private final TimeGraphTreeElement fTreeElement;
    private final Iterable<TimeGraphDrawnEvent> fEvents;

    public TimeGraphDrawnEventRender(TimeRange timeRange,
            TimeGraphTreeElement treeElement,
            Iterable<TimeGraphDrawnEvent> events) {

        fTimeRange = timeRange;
        fTreeElement = treeElement;
        fEvents = ImmutableList.copyOf(events);
    }

    public TimeRange getTimeRange() {
        return fTimeRange;
    }

    public TimeGraphTreeElement getTreeElement() {
        return fTreeElement;
    }

    public Iterable<TimeGraphDrawnEvent> getEvents() {
        return fEvents;
    }
}
