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
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.context.ViewGroupContext;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProvider;
import org.lttng.scope.tmf2.views.core.timegraph.view.TimeGraphModelView;

import javafx.beans.value.ChangeListener;

public final class TimeGraphModelControl {

    private final ChangeListener<TimeRange> fVisibleRangeChangeListener = (obs, oldRange, newRange) -> seekVisibleRange(newRange);

    private final ViewGroupContext fViewContext;
    private final ITimeGraphModelProvider fRenderProvider;

    private @Nullable TimeGraphModelView fView = null;

    public TimeGraphModelControl(ViewGroupContext viewContext, ITimeGraphModelProvider renderProvider) {
        fViewContext = viewContext;
        fRenderProvider = renderProvider;

        attachListeners(viewContext);
    }

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

    public ViewGroupContext getViewContext() {
        return fViewContext;
    }

    public ITimeGraphModelProvider getModelRenderProvider() {
        return fRenderProvider;
    }

    // ------------------------------------------------------------------------
    // Control -> View operations
    // ------------------------------------------------------------------------

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

    public void updateTimeRangeSelection(TimeRange newSelectionRange) {
        fViewContext.setCurrentSelectionTimeRange(newSelectionRange);
    }

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
        if (range.getStart() == Long.MAX_VALUE) {
            throw new IllegalArgumentException("You are trying to make me believe the range starts at " + //$NON-NLS-1$
                    range.getStart() + ". I do not believe you."); //$NON-NLS-1$
        }
        if (range.getEnd() == Long.MAX_VALUE) {
            throw new IllegalArgumentException("You are trying to make me believe the range ends at " + //$NON-NLS-1$
                    range.getEnd() + ". I do not believe you."); //$NON-NLS-1$
        }
    }

    private void checkWindowTimeRange(TimeRange windowRange) {
        checkTimeRange(windowRange);
        TimeRange fullRange = fViewContext.getCurrentTraceFullRange();

        if (windowRange.getStart() < fullRange.getStart()) {
            throw new IllegalArgumentException("Requested window start time: " + windowRange.getStart() +
                    " is smaller than trace start time " + fullRange.getStart());
        }
        if (windowRange.getEnd() > fullRange.getEnd()) {
            throw new IllegalArgumentException("Requested window end time: " + windowRange.getEnd() +
                    " is greater than trace end time " + fullRange.getEnd());
        }
    }

}
