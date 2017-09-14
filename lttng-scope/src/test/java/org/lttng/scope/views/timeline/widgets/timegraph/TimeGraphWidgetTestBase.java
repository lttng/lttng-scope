/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.views.timegraph.control.TimeGraphModelControl;
import com.sun.javafx.tk.TKPulseListener;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.lttng.scope.views.jfx.JfxUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Base for {@link TimeGraphWidget} tests, which sets up all the needed
 * fixtures.
 */
public abstract class TimeGraphWidgetTestBase {

    @Rule
    public TestRule timeoutRule = new Timeout(1, TimeUnit.MINUTES);

    private static StubProject sfProject;
    private static StubView sfView;
    private static TimeGraphWidget sfWidget;
    private static TimeGraphModelControl sfControl;

    private static Stage stage;

    /** Class initialization */
    @BeforeClass
    public static void setupClass() {
        StubTrace trace = new StubTrace();
        StubProject stubProject = new StubProject(trace);

        /* Initialize JavaFX */
        new JFXPanel();
        Platform.setImplicitExit(false);

        StubView view = new StubView();
        TimeGraphModelControl control = view.getControl();
        TimeGraphWidget viewer = view.getViewer();
        assertNotNull(viewer);

        JfxUtils.runLaterAndWait(() -> {
            stage = new Stage();
            stage.setScene(new Scene(viewer.getRootNode()));
            stage.show();

            /* Make sure the window has some reasonable dimensions. */
            stage.setHeight(500);
            stage.setWidth(1600);
        });
        updateUI();

        /* Disable automatic redraw. We'll trigger view painting manually. */
        viewer.getDebugOptions().isPaintingEnabled.set(false);
        /* Disable mouse listeners in case the mouse dwells inside the view. */
        viewer.getDebugOptions().isScrollingListenersEnabled.set(false);

        updateUI();

        control.getViewContext().setCurrentTraceProject(stubProject.getTraceProject());
        updateUI();

        sfProject = stubProject;
        sfView = view;
        sfWidget = viewer;
        sfControl = control;
    }

    /** Class teardown */
    @AfterClass
    public static void teardownClass() {
        if (stage != null) {
            JfxUtils.runLaterAndWait(stage::close);
        }

        if (sfView != null) {
            /* Disposing the view disposes everything underneath */
            sfView.dispose();
        }

        if (sfProject != null) {
            sfProject.close();
        }

        sfView = null;
        sfProject = null;
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
        while (!getWidget().waitForRepaint()) {}
        updateUI();
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
        // TODO Replace with Scene.addPostLayoutListener(), etc. in JavaFX 9
        WaitForNextPulseListener listener = new WaitForNextPulseListener();
        listener.await();
    }

    private static class WaitForNextPulseListener implements TKPulseListener {

        private final CountDownLatch latch;
        private final Toolkit tk;

        private WaitForNextPulseListener() {
            this.latch = new CountDownLatch(2);
            this.tk = Toolkit.getToolkit();
            tk.addPostSceneTkPulseListener(this);
        }

        @Override
        public void pulse() {
            latch.countDown();
            if (latch.getCount() <= 0) {
                tk.removePostSceneTkPulseListener(this);
            }
            tk.requestNextPulse();
        }

        public void await() {
            tk.requestNextPulse();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

        /* We will tolerate being off by a few pixels, due to rounding. */
        double delta = widget.getCurrentNanosPerPixel() * 2;

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
