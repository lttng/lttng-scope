/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;
import org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.Position.HorizontalPosition;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SwtJfxTimeGraphViewerTest {

    private static @Nullable ITmfTrace sfTrace;
    private static @Nullable Display sfDisplay;
    private static @Nullable SwtJfxTimeGraphViewStub sfView;
    private static @Nullable SwtJfxTimeGraphViewer sfViewer;
    private static @Nullable TimeGraphModelControl sfControl;

    @BeforeClass
    public static void setupClass() {
        sfTrace = new TraceFixture();

        SwtJfxTimeGraphViewStub view;
        try {
             view = (SwtJfxTimeGraphViewStub) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(SwtJfxTimeGraphViewStub.VIEW_ID);
        } catch (PartInitException e) {
            fail(e.getMessage());
            throw new RuntimeException(e);
        }

        Display display = view.getViewSite().getShell().getDisplay();
        assertNotNull(display);
        sfDisplay = display;

        TimeGraphModelControl control = view.getControl();
        SwtJfxTimeGraphViewer viewer = view.getViewer();
        assertNotNull(viewer);

        /* Disable mouse listeners in case the mouse dwells inside the view. */
        viewer.getDebugOptions().setScrollingListenersEnabled(false);

        updateUI();
        control.initializeForTrace(sfTrace);
        updateUI();

        sfView = view;
        sfViewer = viewer;
        sfControl = control;
    }

    @AfterClass
    public static void teardownClass() {
        if (sfView != null) {
            /* Disposing the view disposes everything underneath */
            sfView.dispose();
        }

        if (sfTrace != null) {
            sfTrace.dispose();
        }

        sfTrace = null;
        sfView = null;
        sfDisplay = null;
        sfViewer = null;
    }

    @Test
    public void test001InitialPosition() {
        SwtJfxTimeGraphViewer viewer = sfViewer;
        assertNotNull(viewer);

        /*
         * The initial range of the trace is from 100000 to 150000, the viewer
         * should be showing that initially.
         */
        final long expectedStart = TraceFixture.FULL_TRACE_START_TIME;
        final long expectedEnd = TraceFixture.FULL_TRACE_START_TIME + TraceFixture.INITIAL_RANGE_OFFSET;

        /* Check the control */
        assertEquals(expectedStart, viewer.getControl().getVisibleTimeRangeStart());
        assertEquals(expectedEnd, viewer.getControl().getVisibleTimeRangeEnd());

        /* Check the view itself */
        HorizontalPosition timeRange = viewer.getTimeGraphEdgeTimestamps(null);
        long tsStart = timeRange.fStartTime;
        long tsEnd = timeRange.fEndTime;

        assertEquals(expectedStart, tsStart);
        assertEquals(expectedEnd, tsEnd);
    }

    @Test
    public void testSeekVisibleRangeBegin() {
        testSeekVisibleRange(100000L, 110000L);
    }

    @Test
    public void testSeekVisibleRange1() {
        testSeekVisibleRange(120000L, 150000L);
    }

    @Test
    public void testSeekVisibleRange2() {
        testSeekVisibleRange(110000L, 180000L);
    }

    @Test
    public void testSeekVisibleRange3() {
        testSeekVisibleRange(150000L, 170000L);
    }

    @Test
    public void testSeekVisibleRangeEnd() {
        testSeekVisibleRange(170000L, 200000L);
    }

    private void testSeekVisibleRange(long startTime, long endTime) {
        SwtJfxTimeGraphViewer viewer = sfViewer;
        TimeGraphModelControl control = sfControl;
        assertNotNull(viewer);
        assertNotNull(control);

        TmfTimeRange range = createTimeRange(startTime, endTime);
        TmfSignal signal = new TmfWindowRangeUpdatedSignal(this, range);

        control.prepareWaitForNextSignal();
        TmfSignalManager.dispatchSignal(signal);
        control.waitForNextSignal();

        updateUI();

        verifyVisibleRange(startTime, endTime, control, viewer);
    }

    @Test
    public void testZoomInBegin() {
        testZoom(100000L, 110000L, true, 1);
    }

    @Test
    public void testZoomIn1() {
        testZoom(120000L, 150000L, true, 1);
    }

    @Test
    public void testZoomIn2() {
        testZoom(150000L, 160000L, true, 2);
    }

    @Test
    public void testZoomInEnd() {
        testZoom(160000L, 200000L, true, 1);
    }

    @Test
    public void testZoomOutBegin() {
        testZoom(100000L, 140000L, false, 1);
    }

    @Test
    public void testZoomOut1() {
        testZoom(160000L, 180000L, false, 1);
    }

    @Test
    public void testZoomOut2() {
        testZoom(140000L, 160000L, false, 3);
    }

    @Test
    public void testZoomOutEnd() {
        testZoom(180000L, 200000L, false, 1);
    }

    private void testZoom(long initialRangeStart, long initialRangeEnd, boolean zoomIn, int nbSteps) {
        SwtJfxTimeGraphViewer viewer = sfViewer;
        TimeGraphModelControl control = sfControl;
        assertNotNull(viewer);
        assertNotNull(control);

        double totalFactor = Math.pow((1.0 + viewer.getDebugOptions().getZoomStep()), nbSteps);
        if (!zoomIn) {
            totalFactor = 1 / totalFactor;
        }
        long initialRange = initialRangeEnd - initialRangeStart;
        double newRange = initialRange * (1.0 / (totalFactor));
        // TODO Support pivot not exactly in the center of the visible range
        double diff = initialRange - newRange; // diff > 0 means a zoom-in, and vice versa

        /* Seek to the initial requested range */
        control.prepareWaitForNextSignal();
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, createTimeRange(initialRangeStart, initialRangeEnd)));
        control.waitForNextSignal();
        updateUI();

        /* Apply zoom action(s) */
        for (int i = 0; i < nbSteps; i++) {
            control.prepareWaitForNextSignal();
            if (zoomIn) {
                viewer.getZoomActions().zoomIn(null);
            } else {
                viewer.getZoomActions().zoomOut(null);
            }
            control.waitForNextSignal();
        }
        updateUI();

        final long expectedStart = Math.max(initialRangeStart + Math.round(diff / 2),
                TraceFixture.FULL_TRACE_START_TIME);
        final long expectedEnd = Math.min(initialRangeEnd -  Math.round(diff / 2),
                TraceFixture.FULL_TRACE_END_TIME);

        verifyVisibleRange(expectedStart, expectedEnd, control, viewer);
    }

    /**
     * Verify that both the control and viewer passed as parameters currently
     * report the expected visible time range.
     */
    private static void verifyVisibleRange(long expectedStart, long expectedEnd,
            TimeGraphModelControl control,SwtJfxTimeGraphViewer viewer) {
        /* Check the control */
        assertEquals(expectedStart, control.getVisibleTimeRangeStart());
        assertEquals(expectedEnd, control.getVisibleTimeRangeEnd());

        /* Check the view itself */
        HorizontalPosition timeRange = viewer.getTimeGraphEdgeTimestamps(null);
        long tsStart = timeRange.fStartTime;
        long tsEnd = timeRange.fEndTime;

        /* We will tolerate being off by at most 1 pixel */
        double delta = viewer.getCurrentNanosPerPixel();

        assertEqualsWithin(expectedStart, tsStart, delta);
        assertEqualsWithin(expectedEnd, tsEnd, delta);
    }

    private static void assertEqualsWithin(long expected, long actual, double delta) {
        String errMsg = "" + actual + " not within margin (" + delta + ") of " + expected;
        assertTrue(errMsg, actual < expected + delta);
        assertTrue(errMsg, actual > expected - delta);
    }

    private static void updateUI() {
        Display display = sfDisplay;
        if (display == null) {
            return;
        }
        while (display.readAndDispatch()) {}
    }

    private static TmfTimeRange createTimeRange(long start, long end) {
        return new TmfTimeRange(TmfTimestamp.fromNanos(start), TmfTimestamp.fromNanos(end));
    }
}
