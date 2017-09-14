/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph;

import com.efficios.jabberwocky.common.TimeRange;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * {@link TimeGraphWidget} test suite testing seeking operations.
 */
public class TimeGraphWidgetSeekTest extends TimeGraphWidgetTestBase {

    @Before
    public void setup() {
        repaint();
    }

    /**
     * Test seeking to a range bordering the trace's start time
     */
    @Test
    public void testSeekVisibleRangeBegin() {
        testSeekVisibleRange(100000L, 110000L);
    }

    /**
     * Test seeking to an arbitrary range
     */
    @Test
    public void testSeekVisibleRange() {
        testSeekVisibleRange(120000L, 150000L);
        testSeekVisibleRange(110000L, 180000L);
        testSeekVisibleRange(150000L, 170000L);
    }

    /**
     * Test seeking to a range bordering the trace's end time
     */
    @Test
    public void testSeekVisibleRangeEnd() {
        testSeekVisibleRange(170000L, 200000L);
    }

    /**
     * Test seeking to the trace's full time range
     */
    @Test
    @Ignore
    // TODO This test fails when run in succession with all the others
    public void testSeekFullRange() {
        testSeekVisibleRange(100000L, 200000L);
    }

    private void testSeekVisibleRange(long startTime, long endTime) {
        TimeRange range = TimeRange.of(startTime, endTime);

        seekVisibleRange(range);
        verifyVisibleRange(range);
    }

}
