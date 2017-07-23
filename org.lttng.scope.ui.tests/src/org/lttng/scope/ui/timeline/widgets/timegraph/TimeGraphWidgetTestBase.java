/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.timeline.widgets.timegraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.views.timegraph.control.TimeGraphModelControl;

/**
 * Base for {@link TimeGraphWidget} tests, which sets up all the needed
 * fixtures.
 */
public abstract class TimeGraphWidgetTestBase {

    private static @Nullable StubProject sfProject;
    private static @Nullable Display sfDisplay;
    private static @Nullable StubView sfView;
    private static @Nullable TimeGraphWidget sfWidget;
    private static @Nullable TimeGraphModelControl sfControl;

    /** Class initialization */
    @BeforeClass
    public static void setupClass() {
        StubTrace trace = new StubTrace();
        sfProject = new StubProject(trace);

        StubView view;
        try {
            view = (StubView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StubView.VIEW_ID);
        } catch (PartInitException e) {
            fail(e.getMessage());
            throw new RuntimeException(e);
        }

        Display display = view.getViewSite().getShell().getDisplay();
        assertNotNull(display);
        sfDisplay = display;

        TimeGraphModelControl control = view.getControl();
        TimeGraphWidget viewer = view.getViewer();
        assertNotNull(viewer);

        /* Disable automatic redraw. We'll trigger view painting manually. */
        viewer.getDebugOptions().isPaintingEnabled.set(false);
        /* Disable mouse listeners in case the mouse dwells inside the view. */
        viewer.getDebugOptions().isScrollingListenersEnabled.set(false);

        updateUI();

        control.getViewContext().setCurrentTraceProject(sfProject);
        updateUI();

        sfView = view;
        sfWidget = viewer;
        sfControl = control;
    }

    /** Class teardown */
    @AfterClass
    public static void teardownClass() {
        /*
         * Close the view in the UI (to make sure the next test re-instantiates
         * it).
         */
        IWorkbenchPage wp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart myView = wp.findView(StubView.VIEW_ID);
        wp.hideView(myView);

        if (sfView != null) {
            /* Disposing the view disposes everything underneath */
            sfView.dispose();
        }

        if (sfProject != null) {
            sfProject.close();
        }

        sfView = null;
        sfProject = null;
        sfDisplay = null;
        sfWidget = null;
        sfControl = null;
    }

    /**
     * Return the control fixture.
     *
     * @return The control
     */
    protected TimeGraphModelControl getControl() {
        TimeGraphModelControl control = sfControl;
        assertNotNull(control);
        return control;
    }

    /**
     * Return the viewer fixture.
     *
     * @return The viewer
     */
    protected TimeGraphWidget getWidget() {
        TimeGraphWidget widget = sfWidget;
        assertNotNull(widget);
        return widget;
    }

    /**
     * Get the horizontal number of visible pixels in the view.
     *
     * @return The time graph's visible width
     */
    protected double getTimeGraphWidth() {
        return getWidget().getTimeGraphScrollPane().getViewportBounds().getWidth();
    }

    /**
     * Repaint the current time range.
     */
    protected void repaint() {
        TimeRange currentRange = getControl().getViewContext().getCurrentVisibleTimeRange();
        renderRange(currentRange);
    }

    /**
     * Seek the view to a given time range and run a paint operation on this
     * range. Note that this method won't touch the vertical position of the
     * widget.
     *
     * @param range
     *            The target time range
     */
    protected void renderRange(TimeRange range) {
        seekVisibleRange(range);

        getWidget().prepareWaitForRepaint();
        getWidget().paintCurrentLocation();
        while (!getWidget().waitForRepaint()) {
            updateUI();
        }
    }

    /**
     * See the timegraph to the given time range.
     *
     * @param timeRange
     *            The target time range
     */
    protected void seekVisibleRange(TimeRange timeRange) {
        TimeGraphModelControl control = sfControl;
        assertNotNull(control);
        control.getViewContext().setCurrentVisibleTimeRange(timeRange);
        updateUI();
    }

    /**
     * Execute all pending UI operations. Since these tests are meant to start
     * on the UI thread, calling this will allow pausing the test and running
     * queued up UI operations.
     */
    protected static void updateUI() {
        Display display = sfDisplay;
        if (display == null) {
            return;
        }
        while (display.readAndDispatch()) {
        }
    }

    /**
     * Verify that both the control and viewer passed as parameters currently
     * report the expected visible time range.
     *
     * @param expectedRange
     *            Expected time range
     */
    protected static void verifyVisibleRange(TimeRange expectedRange) {
        TimeGraphModelControl control = sfControl;
        TimeGraphWidget widget = sfWidget;
        assertNotNull(control);
        assertNotNull(widget);

        /* Check the control */
        assertEquals(expectedRange, control.getViewContext().getCurrentVisibleTimeRange());

        /* Check the view itself */
        TimeRange timeRange = widget.getTimeGraphEdgeTimestamps(null);
        long tsStart = timeRange.getStartTime();
        long tsEnd = timeRange.getEndTime();

        /* We will tolerate being off by at most 1 pixel */
        double delta = widget.getCurrentNanosPerPixel();

        assertEqualsWithin(expectedRange.getStartTime(), tsStart, delta);
        assertEqualsWithin(expectedRange.getEndTime(), tsEnd, delta);
    }

    /**
     *
     * Assert that a long value is equal to another, within a given delta.
     *
     * @param expected
     *            The expected value
     * @param actual
     *            The value to test
     * @param delta
     *            The delta
     */
    protected static void assertEqualsWithin(long expected, long actual, double delta) {
        String errMsg = "" + actual + " not within margin (" + delta + ") of " + expected;
        assertTrue(errMsg, actual < expected + delta);
        assertTrue(errMsg, actual > expected - delta);
    }

}
