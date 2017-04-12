/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;

/**
 * Base for {@link TimeGraphWidget} tests, which sets up all the needed
 * fixtures.
 */
public abstract class TimeGraphWidgetTestBase {

    private static @Nullable ITmfTrace sfTrace;
    private static @Nullable Display sfDisplay;
    private static @Nullable StubView sfView;
    private static @Nullable TimeGraphWidget sfWidget;
    private static @Nullable TimeGraphModelControl sfControl;

    /** Class initialization */
    @BeforeClass
    public static void setupClass() {
        sfTrace = new StubTrace();

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
        control.getViewContext().setCurrentTrace(sfTrace);
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

        if (sfTrace != null) {
            sfTrace.dispose();
        }

        sfView = null;
        sfTrace = null;
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

    protected void seekVisibleRange(TimeRange timeRange) {
        TimeGraphModelControl control = sfControl;
        assertNotNull(control);
        control.getViewContext().setCurrentVisibleTimeRange(timeRange);
        updateUI();
    }

}
