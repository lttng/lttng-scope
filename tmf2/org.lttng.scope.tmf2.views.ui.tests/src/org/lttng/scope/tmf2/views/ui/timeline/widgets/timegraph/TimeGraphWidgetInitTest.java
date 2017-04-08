/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.lttng.scope.tmf2.views.core.TimeRange;

/**
 * {@link TimeGraphWidget} test checking the initial conditions.
 */
public class TimeGraphWidgetInitTest extends TimeGraphWidgetTestBase {

    /**
     * Test that the initial visible position of both the control and the view
     * are as expected by the trace's configuration.
     */
    @Test
    public void testInitialPosition() {
        TimeGraphWidget viewer = getWidget();

        /*
         * The initial range of the trace is from 100000 to 150000, the viewer
         * should be showing that initially.
         */
        final long expectedStart = StubTrace.FULL_TRACE_START_TIME;
        final long expectedEnd = StubTrace.FULL_TRACE_START_TIME + StubTrace.INITIAL_RANGE_OFFSET;

        /* Check the control */
        TimeRange visibleRange = viewer.getControl().getViewContext().getCurrentVisibleTimeRange();
        assertEquals(expectedStart, visibleRange.getStart());
        assertEquals(expectedEnd, visibleRange.getEnd());

        /* Check the view itself */
        TimeRange timeRange = viewer.getTimeGraphEdgeTimestamps(null);
        assertEquals(expectedStart, timeRange.getStart());
        assertEquals(expectedEnd, timeRange.getEnd());
    }
}
