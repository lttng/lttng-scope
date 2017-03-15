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
import org.junit.Ignore;
import org.junit.Test;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;
import org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.Position.HorizontalPosition;

public class SwtJfxTimeGraphViewerTest {

    private static @Nullable ITmfTrace sfTrace;
    private static @Nullable SwtJfxTimeGraphViewStub sfView;
    private static @Nullable Display sfDisplay;
    private static @Nullable SwtJfxTimeGraphViewer sfViewer;

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

        updateUI();
        control.initializeForTrace(sfTrace);
        updateUI();

        sfView = view;
        sfViewer = viewer;
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
    public void testInitialPosition() {
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
    @Ignore("not yet functional")
    public void testSeekVisibleRange() throws InterruptedException {
        SwtJfxTimeGraphViewer viewer = sfViewer;
        assertNotNull(viewer);

        final long startTime = 150000L;
        final long endTime = 160000L;

        viewer.getTimeGraphScrollPane().hvalueProperty().addListener((observable, oldVal, newVal) -> {
            System.out.println("old hvalue=" + oldVal + ", new hvalue=" + newVal);
            (new Throwable()).printStackTrace();
        });

        TmfTimeRange range = createTimeRange(startTime, endTime);
        TmfSignal signal = new TmfWindowRangeUpdatedSignal(this, range);
        TmfSignalManager.dispatchSignal(signal);

        /* Check the control */
        assertEquals(startTime, viewer.getControl().getVisibleTimeRangeStart());
        assertEquals(endTime, viewer.getControl().getVisibleTimeRangeEnd());

        /* Check the view itself */
        HorizontalPosition timeRange = viewer.getTimeGraphEdgeTimestamps(null);
        long tsStart = timeRange.fStartTime;
        long tsEnd = timeRange.fEndTime;

        assertEquals(startTime, tsStart);
        assertEquals(endTime, tsEnd);
    }

    @Test
    public void testZoomOut() {

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
