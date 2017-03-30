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
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.view.TimeGraphModelView;

import com.google.common.annotations.VisibleForTesting;

public final class TimeGraphModelControl {

    /** Value representing uninitialized timestamps */
    public static final TimeRange UNINITIALIZED = TimeRange.of(0, 0);

    private final ITimeGraphModelRenderProvider fRenderProvider;
    private final SignallingContext fSignallingContext;

    private @Nullable TimeGraphModelView fView = null;
    private @Nullable ITmfTrace fCurrentTrace = null;

    private TimeRange fFullTimeGraphRange = UNINITIALIZED;
    private TimeRange fLatestVisibleRange = UNINITIALIZED;

    public TimeGraphModelControl(ITimeGraphModelRenderProvider renderProvider) {
        fRenderProvider = renderProvider;
        fSignallingContext = new SignallingContext(this);
    }

    public void attachView(TimeGraphModelView view) {
        fView = view;
    }

    @Nullable TimeGraphModelView getView() {
        return fView;
    }

    public void dispose() {
        if (fView != null) {
            fView.dispose();
        }
        fSignallingContext.dispose();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Nullable ITmfTrace getCurrentTrace() {
        return fCurrentTrace;
    }

    /**
     * Recompute the total virtual size of the time graph area, and assigns the
     * given timestamps as the start and end positions.
     *
     * All subsquent operations (seek, paint, etc.) that use timestamp expect
     * these timestamps to be within the range passed here!
     *
     * Should be called when the trace changes, or the trace's total time range
     * is updated (while indexing, or in live cases).
     */
    void setFullTimeRange(TimeRange fullAreaRange) {
        checkTimeRange(fullAreaRange);
        fFullTimeGraphRange = fullAreaRange;
    }

    public TimeRange getFullTimeGraphRange() {
        return fFullTimeGraphRange;
    }

    void setVisibleTimeRange(TimeRange newVisibleRange) {
        checkTimeRange(newVisibleRange);
        fLatestVisibleRange = newVisibleRange;
    }

    public TimeRange getVisibleTimeRange() {
        return fLatestVisibleRange;
    }

    public ITimeGraphModelRenderProvider getModelRenderProvider() {
        return fRenderProvider;
    }

    // ------------------------------------------------------------------------
    // Control -> View operations
    // ------------------------------------------------------------------------

    public synchronized void initializeForTrace(@Nullable ITmfTrace trace) {
        fCurrentTrace = trace;
        fRenderProvider.setTrace(trace);

        TimeGraphModelView view = fView;
        if (view != null) {
            view.clear();
        }

        if (trace == null) {
            /* View will remain cleared, good */
            return;
        }

        long traceStartTime = trace.getStartTime().toNanos();
        long traceEndTime = trace.getEndTime().toNanos();

        /* The window's start time is the same as the trace's start time initially */
        long windowStartTime = traceStartTime;
        long windowEndtime = Math.min(traceEndTime, traceStartTime + trace.getInitialRangeOffset().toNanos());

        setFullTimeRange(TimeRange.of(traceStartTime, traceEndTime));
        seekVisibleRange(TimeRange.of(windowStartTime, windowEndtime));
    }

    public void repaintCurrentArea() {
        ITmfTrace trace = fCurrentTrace;
        TimeRange currentRange = fLatestVisibleRange;
        if (trace == null || currentRange == UNINITIALIZED) {
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
        setVisibleTimeRange(newRange);

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
        fSignallingContext.sendTimeRangeSelectionUpdate(newSelectionRange);
    }

    public void updateVisibleTimeRange(TimeRange newVisibleRange, boolean echo) {
        checkTimeRange(newVisibleRange);
        fSignallingContext.sendVisibleWindowRangeUpdate(newVisibleRange, echo);
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

        if (windowRange.getStart() < fFullTimeGraphRange.getStart()) {
            throw new IllegalArgumentException("Requested window start time: " + windowRange.getStart() +
                    " is smaller than trace start time " + fFullTimeGraphRange.getStart());
        }
        if (windowRange.getEnd() > fFullTimeGraphRange.getEnd()) {
            throw new IllegalArgumentException("Requested window end time: " + windowRange.getEnd() +
                    " is greater than trace end time " + fFullTimeGraphRange.getEnd());
        }
    }

    // ------------------------------------------------------------------------
    // Test utils
    // ------------------------------------------------------------------------

    @VisibleForTesting
    public void prepareWaitForNextSignal() {
        fSignallingContext.prepareWaitForNextSignal();
    }

    @VisibleForTesting
    public void waitForNextSignal() {
        fSignallingContext.waitForNextSignal();
    }
}
