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
import org.junit.Test;
import org.lttng.scope.common.tests.JfxTestUtils;
import org.lttng.scope.common.tests.StubTrace;

/**
 * {@link TimeGraphWidget} test suite testing zooming operations.
 */
public class TimeGraphWidgetZoomTest extends TimeGraphWidgetTestBase {

    /**
     * Test zooming-in, starting from a range bordering the trace's start time.
     */
    @Test
    public void testZoomInBegin() {
        testZoom(100000L, 110000L, 105000L, true, 1);
        testZoom(100000L, 120000L, 110000L, true, 3);
    }

    /**
     * Test zooming-in, starting from an arbitrary range.
     */
    @Test
    public void testZoomIn() {
        testZoom(120000L, 150000L, 135000L, true, 1);
        testZoom(150000L, 160000L, 155000L, true, 2);
    }

    /**
     * Test zooming-in, starting from a range bordering the trace's end time.
     */
    @Test
    public void testZoomInEnd() {
        testZoom(160000L, 200000L, 180000L, true, 1);
        testZoom(180000L, 200000L, 190000L, true, 3);
    }

    /**
     * Test zooming-in, starting from the trace's full time range.
     */
    @Test
    public void testZoomInFull() {
        testZoom(100000L, 200000L, 150000L, true, 1);
        testZoom(100000L, 200000L, 150000L, true, 3);
    }

    /**
     * Test zooming-out, starting from a range bordering the trace's start time.
     * The resulting range should be clamped to the trace's start time.
     */
    @Test
    public void testZoomOutBegin() {
        testZoom(100000L, 140000L, 120000L, false, 1);
    }

    /**
     * Test zooming-out, starting from an arbitrary range.
     */
    @Test
    public void testZoomOut() {
        testZoom(160000L, 180000L, 170000L, false, 1);
        testZoom(140000L, 160000L, 150000L, false, 3);
    }

    /**
     * Test zooming-out, starting from a range bordering the trace's end time.
     * The resulting range should be clamped to the trace's end time.
     */
    @Test
    public void testZoomOutEnd() {
        testZoom(180000L, 200000L, 190000L, false, 1);
    }

    /**
     * Test zooming-out, starting from the trace's full time range. The visible
     * range should not be modified.
     */
    @Test
    public void testZoomOutFull() {
        testZoom(160000L, 200000L, 180000L, true, 1);
        testZoom(180000L, 200000L, 190000L, true, 3);
    }

    private void testZoom(long initialRangeStart, long initialRangeEnd, long zoomPivot, boolean zoomIn, int nbSteps) {
        TimeRange initialRange = TimeRange.of(initialRangeStart, initialRangeEnd);
        TimeRange selectionRange = TimeRange.of(zoomPivot, zoomPivot);
        TimeGraphWidget widget = getWidget();

        seekVisibleRange(initialRange);

        widget.getViewContext().setSelectionTimeRange(selectionRange);

        double totalFactor = Math.pow((1.0 + widget.getDebugOptions().zoomStep.get()), nbSteps);
        if (!zoomIn) {
            totalFactor = 1 / totalFactor;
        }
        long initialRangeDuration = initialRange.getDuration();
        double newRangeDuration = initialRangeDuration * (1.0 / (totalFactor));

        double durationDelta = newRangeDuration - initialRangeDuration;
        double zoomPivotRatio = (double) (zoomPivot - initialRange.getStartTime()) / (double) (initialRange.getDuration());

        long newStart = initialRange.getStartTime() - Math.round(durationDelta * zoomPivotRatio);
        long newEnd = initialRange.getEndTime() + Math.round(durationDelta - (durationDelta * zoomPivotRatio));

        /* Apply zoom action(s) */
        for (int i = 0; i < nbSteps; i++) {
            widget.getZoomActions().zoom(zoomIn, false, null);
        }
        JfxTestUtils.updateUI();

        final long expectedStart = Math.max(
                newStart, StubTrace.FULL_TRACE_START_TIME);
        final long expectedEnd = Math.min(
                newEnd, StubTrace.FULL_TRACE_END_TIME);
        TimeRange expectedRange = TimeRange.of(expectedStart, expectedEnd);

        verifyVisibleRange(expectedRange);
    }

}
