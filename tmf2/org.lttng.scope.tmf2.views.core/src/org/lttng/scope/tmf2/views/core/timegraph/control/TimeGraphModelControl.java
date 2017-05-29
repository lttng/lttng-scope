/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.control;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.context.ViewGroupContext;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProvider;
import org.lttng.scope.tmf2.views.core.timegraph.view.TimeGraphModelView;

import com.efficios.jabberwocky.common.TimeRange;

import javafx.beans.value.ChangeListener;

/**
 * Control part of the timegraph MVC mechanism. It links a
 * {@link TimeGraphModelView} to a {@link ITimeGraphModelProvider}.
 *
 * @author Alexandre Montplaisir
 */
public final class TimeGraphModelControl {

    private final ChangeListener<TimeRange> fVisibleRangeChangeListener = (obs, oldRange, newRange) -> seekVisibleRange(newRange);

    private final ViewGroupContext fViewContext;
    private final ITimeGraphModelProvider fRenderProvider;

    private @Nullable TimeGraphModelView fView = null;

    /**
     * Constructor.
     *
     * The control links a model provider, and a view. But the view also needs a
     * back-reference to the control. The suggested pattern is to do:
     *
     * <pre>
     * ITimeGraphModelProvider provider = ...
     * TimeGraphModelControl control = new TimeGraphModelControl(viewContext, provider);
     * TimeGraphModelView view = new TimeGraphModelView(control);
     * control.attachView(view);
     * </pre>
     *
     * @param viewContext
     *            The view context to which this timegraph belongs
     * @param provider
     *            The model provider that goes with this control
     */
    public TimeGraphModelControl(ViewGroupContext viewContext, ITimeGraphModelProvider provider) {
        fViewContext = viewContext;
        fRenderProvider = provider;

        attachListeners(viewContext);
    }

    /**
     * Attach a view to this control
     *
     * @param view
     *            The view to attach
     */
    public void attachView(TimeGraphModelView view) {
        fView = view;

        /*
         * Initially populate the view with the context of the current trace.
         */
        ITmfTrace trace = getViewContext().getCurrentTrace();
        initializeForTrace(trace);
    }

    @Nullable TimeGraphModelView getView() {
        return fView;
    }

    /**
     * Dispose this control and its components.
     */
    public void dispose() {
        if (fView != null) {
            fView.dispose();
        }
    }

    private void attachListeners(ViewGroupContext viewContext) {
        viewContext.currentTraceProperty().addListener((observable, oldTrace, newTrace) -> {
            initializeForTrace(newTrace);

            viewContext.currentSelectionTimeRangeProperty().addListener((obs, oldRange, newRange) -> {
                drawSelection(newRange);
            });

            viewContext.currentVisibleTimeRangeProperty().addListener(fVisibleRangeChangeListener);
        });
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Get the view context to which this control belongs.
     *
     * @return The view context
     */
    public ViewGroupContext getViewContext() {
        return fViewContext;
    }

    /**
     * Get the model provider of this control
     *
     * @return The model provider
     */
    public ITimeGraphModelProvider getModelRenderProvider() {
        return fRenderProvider;
    }

    // ------------------------------------------------------------------------
    // Control -> View operations
    // ------------------------------------------------------------------------

    /**
     * Initialize this timegraph for a new trace.
     *
     * @param trace
     *            The trace to initialize in the view. If it is null it indcates
     *            'no trace'.
     */
    public synchronized void initializeForTrace(@Nullable ITmfTrace trace) {
        fRenderProvider.setTrace(trace);

        TimeGraphModelView view = fView;
        if (view == null) {
            return;
        }
        view.clear();

        if (trace == null) {
            /* View will remain cleared, good */
            return;
        }

        TimeRange currentVisibleRange = fViewContext.getCurrentVisibleTimeRange();
        checkWindowTimeRange(currentVisibleRange);
        view.seekVisibleRange(currentVisibleRange);
    }

    /**
     * Repaint, without seeking anywhere else, the current displayed area of the
     * view.
     *
     * This can be called whenever some settings like filters etc. have changed,
     * so that a repaint will show updated information.
     */
    public void repaintCurrentArea() {
        ITmfTrace trace = fViewContext.getCurrentTrace();
        TimeRange currentRange = fViewContext.getCurrentVisibleTimeRange();
        if (trace == null || currentRange == ViewGroupContext.UNINITIALIZED_RANGE) {
            return;
        }

        TimeGraphModelView view = fView;
        if (view != null) {
            view.clear();
            view.seekVisibleRange(currentRange);
        }
    }

    void seekVisibleRange(TimeRange newRange) {
        checkWindowTimeRange(newRange);
        TimeGraphModelView view = fView;
        if (view != null) {
            view.seekVisibleRange(newRange);
        }
    }

    void drawSelection(TimeRange selectionRange) {
        checkWindowTimeRange(selectionRange);
        TimeGraphModelView view = fView;
        if (view != null) {
            view.drawSelection(selectionRange);
        }
    }



    // ------------------------------------------------------------------------
    // View -> Control operations (Control external API)
    // ------------------------------------------------------------------------

    /**
     * Change the current time range selection.
     *
     * Called by the view to indicate that the user has input a new time range
     * selection from the view. The control will relay this to the rest of the
     * framework.
     *
     * @param newSelectionRange
     *            The new time range selection.
     */
    public void updateTimeRangeSelection(TimeRange newSelectionRange) {
        fViewContext.setCurrentSelectionTimeRange(newSelectionRange);
    }

    /**
     * Change the current visible time range.
     *
     * Called by the view whenever the user selects a new visible time range,
     * for example by scrolling left or right.
     *
     * @param newVisibleRange
     *            The new visible time range
     * @param echo
     *            This flag indicates if the view wants to receive the new time
     *            range notification back to itself (via
     *            {@link #seekVisibleRange}.
     */
    public void updateVisibleTimeRange(TimeRange newVisibleRange, boolean echo) {
        checkTimeRange(newVisibleRange);

        /*
         * If 'echo' is 'off', we will avoid triggering our change listener on
         * this modification by detaching then re-attaching it afterwards.
         */
        if (echo) {
            fViewContext.setCurrentVisibleTimeRange(newVisibleRange);
        } else {
            fViewContext.currentVisibleTimeRangeProperty().removeListener(fVisibleRangeChangeListener);
            fViewContext.setCurrentVisibleTimeRange(newVisibleRange);
            fViewContext.currentVisibleTimeRangeProperty().addListener(fVisibleRangeChangeListener);
        }
    }

    // ------------------------------------------------------------------------
    // Utils
    // ------------------------------------------------------------------------

    private static void checkTimeRange(TimeRange range) {
        if (range.getStartTime() == Long.MAX_VALUE) {
            throw new IllegalArgumentException("You are trying to make me believe the range starts at " + //$NON-NLS-1$
                    range.getStartTime() + ". I do not believe you."); //$NON-NLS-1$
        }
        if (range.getEndTime() == Long.MAX_VALUE) {
            throw new IllegalArgumentException("You are trying to make me believe the range ends at " + //$NON-NLS-1$
                    range.getEndTime() + ". I do not believe you."); //$NON-NLS-1$
        }
    }

    private void checkWindowTimeRange(TimeRange windowRange) {
        checkTimeRange(windowRange);
        TimeRange fullRange = fViewContext.getCurrentTraceFullRange();

        if (windowRange.getStartTime() < fullRange.getStartTime()) {
            throw new IllegalArgumentException("Requested window start time: " + windowRange.getStartTime() + //$NON-NLS-1$
                    " is smaller than trace start time " + fullRange.getStartTime()); //$NON-NLS-1$
        }
        if (windowRange.getEndTime() > fullRange.getEndTime()) {
            throw new IllegalArgumentException("Requested window end time: " + windowRange.getEndTime() + //$NON-NLS-1$
                    " is greater than trace end time " + fullRange.getEndTime()); //$NON-NLS-1$
        }
    }

}
