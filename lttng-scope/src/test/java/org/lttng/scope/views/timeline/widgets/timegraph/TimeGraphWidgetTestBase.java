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
import com.efficios.jabberwocky.tests.JavaFXTestBase;
import com.efficios.jabberwocky.views.timegraph.control.TimeGraphModelControl;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.lttng.scope.common.jfx.JfxUtils;
import org.lttng.scope.common.tests.JfxTestUtils;
import org.lttng.scope.common.tests.StubProject;
import org.lttng.scope.common.tests.StubTrace;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base for {@link TimeGraphWidget} tests, which sets up all the needed
 * fixtures.
 */
public abstract class TimeGraphWidgetTestBase extends JavaFXTestBase {

    private static StubProject sfProject;
    private static StubView sfView;
    private static TimeGraphWidget sfWidget;
    private static TimeGraphModelControl sfControl;

    private static Stage stage;

    /**
     * Class initialization
     */
    @BeforeAll
    public static void setupClass() {
        StubTrace trace = new StubTrace();
        StubProject stubProject = new StubProject(trace);

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
        JfxTestUtils.updateUI();

        /* Disable automatic redraw. We'll trigger view painting manually. */
        viewer.getDebugOptions().isPaintingEnabled.set(false);
        /* Disable mouse listeners in case the mouse dwells inside the view. */
        viewer.getDebugOptions().isScrollingListenersEnabled.set(false);

        JfxTestUtils.updateUI();

        control.getViewContext().switchProject(stubProject.getTraceProject());
        JfxTestUtils.updateUI();

        sfProject = stubProject;
        sfView = view;
        sfWidget = viewer;
        sfControl = control;
    }

    /**
     * Class teardown
     */
    @AfterAll
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
        TimeRange currentRange = getControl().getViewContext().getVisibleTimeRange();
        renderRange(currentRange);
    }

    /**
     * Seek the view to a given time range and run a paint operation on this
     * range. Note that this method won't touch the vertical position of the
     * widget.
     *
     * @param range The target time range
     */
    protected void renderRange(TimeRange range) {
        seekVisibleRange(range);

        getWidget().prepareWaitForRepaint();
        getWidget().paintCurrentLocation();
        while (!getWidget().waitForRepaint()) {
        }
        JfxTestUtils.updateUI();
    }

    /**
     * See the timegraph to the given time range.
     *
     * @param timeRange The target time range
     */
    protected void seekVisibleRange(TimeRange timeRange) {
        TimeGraphModelControl control = sfControl;
        assertNotNull(control);
        control.getViewContext().setVisibleTimeRange(timeRange);
        JfxTestUtils.updateUI();
    }

    /**
     * Verify that both the control and viewer passed as parameters currently
     * report the expected visible time range.
     *
     * @param expectedRange Expected time range
     */
    protected static void verifyVisibleRange(TimeRange expectedRange) {
        TimeGraphModelControl control = sfControl;
        TimeGraphWidget widget = sfWidget;
        assertNotNull(control);
        assertNotNull(widget);

        /* Check the control */
        assertEquals(expectedRange, control.getViewContext().getVisibleTimeRange());

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
     * Assert that a long value is equal to another, within a given delta.
     *
     * @param expected The expected value
     * @param actual   The value to test
     * @param delta    The delta
     */
    protected static void assertEqualsWithin(long expected, long actual, double delta) {
        String errMsg = "" + actual + " not within margin (" + delta + ") of " + expected;
        assertAll(
                () -> assertTrue(actual < expected + delta, errMsg),
                () -> assertTrue(actual > expected - delta, errMsg));
    }

}
