/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.view;

import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.context.ViewGroupContext;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;

/**
 * Base class for time graph view objects.
 *
 * A view is attached to a {@link TimeGraphModelControl} (passed at the
 * constructor), which will then call the view's method accordingly to
 * reposition, repaint, etc. the timegraph according to actions taken elsewhere
 * in the framework.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TimeGraphModelView {

    private final TimeGraphModelControl fControl;

    /**
     * Constructor. Build a new view by specifying its corresponding control.
     * You will most probably want to call
     * {@link TimeGraphModelControl#attachView} afterwards, this is not done
     * automatically!
     *
     * @param control
     *            The control that will manage this view
     */
    public TimeGraphModelView(TimeGraphModelControl control) {
        fControl = control;
    }

    /**
     * Retrieve the control managing this view.
     *
     * @return The model control
     */
    public final TimeGraphModelControl getControl() {
        return fControl;
    }

    /**
     * Dispose this view
     */
    public final void dispose() {
        disposeImpl();
    }

    /**
     * Get the view context of this view/control.
     *
     * @return The current view context
     */
    public final ViewGroupContext getViewContext() {
        return getControl().getViewContext();
    }

    // ------------------------------------------------------------------------
    // Template methods
    // ------------------------------------------------------------------------

    /**
     * Abstract implementation of a dispose method. Sub-classes should clean up
     * the state they add to the base view class.
     */
    protected abstract void disposeImpl();

    /**
     * Request the timegraph to be completely cleared of its contents, for
     * example when no trace is opened at all.
     */
    public abstract void clear();

    /**
     * This should be called whenever the visible window moves, including zoom
     * level changes.
     *
     * @param newVisibleRange
     *            The range to where the view should be seeked
     */
    public abstract void seekVisibleRange(TimeRange newVisibleRange);

    /**
     * Draw a new selection rectangle. The previous one, if any, will be
     * removed.
     *
     * @param selectionRange
     *            The selection that should be drawn
     */
    public abstract void drawSelection(TimeRange selectionRange);

}
