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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;

/**
 * {@link TimeGraphWidget} test suite testing seeking and zooming operations.
 */
public class TimeGraphWidgetSeekTest extends TimeGraphWidgetTestBase {

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
    public void testSeekFullRange() {
        testSeekVisibleRange(100000L, 200000L);
    }

    private void testSeekVisibleRange(long startTime, long endTime) {
        TimeRange range = TimeRange.of(startTime, endTime);
        TimeGraphWidget viewer = getWidget();
        TimeGraphModelControl control = getControl();

        seekVisibleRange(range);
        verifyVisibleRange(range, control, viewer);
    }

    /**
     * Test zooming-in, starting from a range bordering the trace's start time.
     */
    @Test
    public void testZoomInBegin() {
        testZoom(100000L, 110000L, true, 1);
        testZoom(100000L, 120000L, true, 3);
    }

    /**
     * Test zooming-in, starting from an arbitrary range.
     */
    @Test
    public void testZoomIn() {
        testZoom(120000L, 150000L, true, 1);
        testZoom(150000L, 160000L, true, 2);
    }

    /**
     * Test zooming-in, starting from a range bordering the trace's end time.
     */
    @Test
    public void testZoomInEnd() {
        testZoom(160000L, 200000L, true, 1);
        testZoom(180000L, 200000L, true, 3);
    }

    /**
     * Test zooming-in, starting from the trace's full time range.
     */
    @Test
    public void testZoomInFull() {
        testZoom(100000L, 200000L, true, 1);
        testZoom(100000L, 200000L, true, 3);
    }

    /**
     * Test zooming-out, starting from a range bordering the trace's start time.
     * The resulting range should be clamped to the trace's start time.
     */
    @Test
    public void testZoomOutBegin() {
        testZoom(100000L, 140000L, false, 1);
    }

    /**
     * Test zooming-out, starting from an arbitrary range.
     */
    @Test
    public void testZoomOut() {
        testZoom(160000L, 180000L, false, 1);
        testZoom(140000L, 160000L, false, 3);
    }

    /**
     * Test zooming-out, starting from a range bordering the trace's end time.
     * The resulting range should be clamped to the trace's end time.
     */
    @Test
    public void testZoomOutEnd() {
        testZoom(180000L, 200000L, false, 1);
    }

    /**
     * Test zooming-out, starting from the trace's full time range. The visible
     * range should not be modified.
     */
    @Test
    public void testZoomOutFull() {
        testZoom(160000L, 200000L, true, 1);
        testZoom(180000L, 200000L, true, 3);
    }

    private void testZoom(long initialRangeStart, long initialRangeEnd, boolean zoomIn, int nbSteps) {
        TimeRange initialRange = TimeRange.of(initialRangeStart, initialRangeEnd);
        TimeGraphWidget viewer = getWidget();
        TimeGraphModelControl control = getControl();

        seekVisibleRange(initialRange);

        double totalFactor = Math.pow((1.0 + viewer.getDebugOptions().zoomStep.get()), nbSteps);
        if (!zoomIn) {
            totalFactor = 1 / totalFactor;
        }
        long initialRangeDuration = initialRange.getDuration();
        double newRangeDuration = initialRangeDuration * (1.0 / (totalFactor));
        // TODO Support pivot not exactly in the center of the visible range
        /* diff > 0 means a zoom-in, and vice versa */
        double diff = initialRangeDuration - newRangeDuration;

        /* Apply zoom action(s) */
        for (int i = 0; i < nbSteps; i++) {
            viewer.getZoomActions().zoom(null, zoomIn);
        }
        updateUI();

        final long expectedStart = Math.max(
                initialRangeStart + Math.round(diff / 2),
                StubTrace.FULL_TRACE_START_TIME);
        final long expectedEnd = Math.min(
                initialRangeEnd - Math.round(diff / 2),
                StubTrace.FULL_TRACE_END_TIME);
        TimeRange expectedRange = TimeRange.of(expectedStart, expectedEnd);

        verifyVisibleRange(expectedRange, control, viewer);
    }

    /**
     * Verify that both the control and viewer passed as parameters currently
     * report the expected visible time range.
     */
    private static void verifyVisibleRange(TimeRange expectedRange,
            TimeGraphModelControl control, TimeGraphWidget viewer) {
        /* Check the control */
        assertEquals(expectedRange, control.getViewContext().getCurrentVisibleTimeRange());

        /* Check the view itself */
        TimeRange timeRange = viewer.getTimeGraphEdgeTimestamps(null);
        long tsStart = timeRange.getStart();
        long tsEnd = timeRange.getEnd();

        /* We will tolerate being off by at most 1 pixel */
        double delta = viewer.getCurrentNanosPerPixel();

        assertEqualsWithin(expectedRange.getStart(), tsStart, delta);
        assertEqualsWithin(expectedRange.getEnd(), tsEnd, delta);
    }

    private static void assertEqualsWithin(long expected, long actual, double delta) {
        String errMsg = "" + actual + " not within margin (" + delta + ") of " + expected;
        assertTrue(errMsg, actual < expected + delta);
        assertTrue(errMsg, actual > expected - delta);
    }

}
