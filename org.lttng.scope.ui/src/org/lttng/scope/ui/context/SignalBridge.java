/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.context;

import org.eclipse.tracecompass.ctf.tmf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.context.ViewGroupContext;
import com.efficios.jabberwocky.project.ITraceProject;

/**
 * Bridge between a {@link ViewGroupContext} and the {@link TmfSignalManager}.
 * It sends equivalent "signals" back-and-forth between the two APIs.
 *
 * Note: Needs to be public because of the way TmfSignalManager works.
 *
 * @author Alexandre Montplaisir
 */
public class SignalBridge {

    private static final int SIGNAL_DELAY_MS = 200;

    private final TmfSignalThrottler fVisibleRangeSignalThrottler = new TmfSignalThrottler(null, SIGNAL_DELAY_MS);

    private final ViewGroupContext fViewContext;

    /**
     * Constructor
     *
     * @param viewContext
     *            The view context this bridge will connect to
     */
    public SignalBridge(ViewGroupContext viewContext) {
        TmfSignalManager.register(this);
        fViewContext = viewContext;

        viewContext.currentTraceProjectProperty().addListener((obs, oldProject, newProject) -> {
            if (newProject == null) {
                return;
            }

            // Currently nothing on the JW side can affect the selected trace/project, so we
            // don't need to send the trace selected signal
//            TmfSignal newTraceSignal = new TmfTraceSelectedSignal(SignalBridge.this, newTrace);
//            TmfSignalManager.dispatchSignal(newTraceSignal);

            /* Synchronize the visible range with TMF */
            viewContext.currentVisibleTimeRangeProperty().addListener((observable, oldRange, newRange) -> {
                TmfTimeRange tmfTimeRange = toTmfTimeRange(newRange);
                TmfSignal signal = new TmfWindowRangeUpdatedSignal(SignalBridge.this, tmfTimeRange);
                fVisibleRangeSignalThrottler.queue(signal);
            });

            /* Synchronize the range selection with TMF */
            viewContext.currentSelectionTimeRangeProperty().addListener((observable, oldRange, newRange) -> {
                TmfSignal signal;
                if (newRange.isSingleTimestamp()) {
                    ITmfTimestamp ts = TmfTimestamp.fromNanos(newRange.getStartTime());
                    signal = new TmfSelectionRangeUpdatedSignal(SignalBridge.this, ts);
                } else {
                    ITmfTimestamp startTs = TmfTimestamp.fromNanos(newRange.getStartTime());
                    ITmfTimestamp endTs = TmfTimestamp.fromNanos(newRange.getEndTime());
                    signal = new TmfSelectionRangeUpdatedSignal(SignalBridge.this, startTs, endTs);
                }
                TmfSignalManager.dispatchSignal(signal);
            });

        });
    }

    /**
     * Dispose of this bridge, deregistering it from the signal manager.
     */
    public void dispose() {
        TmfSignalManager.deregister(this);
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the trace selected signal
     *
     * @param signal
     *            Received signal
     */
    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        if (signal.getSource() == this) {
            return;
        }

        ITraceProject<?, ?> project;
        ITmfTrace trace = signal.getTrace();
        if (trace instanceof CtfTmfTrace) {
            project = ((CtfTmfTrace) trace).getJwProject();
        } else {
            project = null;
        }
        fViewContext.setCurrentTraceProject(project);
    }

    /**
     * Handler for the trace closed signal
     *
     * @param signal
     *            Received signal
     */
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
        if (signal.getSource() == this) {
            return;
        }

        /* Set the current project to null if there is no more active trace */
        if (TmfTraceManager.getInstance().getActiveTrace() == null) {
            fViewContext.setCurrentTraceProject(null);
        }
    }

    /**
     * Handler for the selection range updated signal
     *
     * @param signal
     *            Received signal
     */
    @TmfSignalHandler
    public void selectionRangeUpdated(final TmfSelectionRangeUpdatedSignal signal) {
        if (signal.getSource() == this) {
            return;
        }
        long rangeStart = signal.getBeginTime().toNanos();
        long rangeEnd = signal.getEndTime().toNanos();

        /* Sometimes the range is weird... */
        if (rangeStart == Long.MAX_VALUE || rangeEnd == Long.MAX_VALUE) {
            return;
        }

        /*
         * This signal's end can be before its start time, against all logic.
         */
        TimeRange range;
        if (rangeStart > rangeEnd) {
            range = TimeRange.of(rangeEnd, rangeStart);
        } else {
            range = TimeRange.of(rangeStart, rangeEnd);
        }

        ITraceProject<?, ?> project = fViewContext.getCurrentTraceProject();
        if (project == null) {
            return;
        }
        fViewContext.setCurrentSelectionTimeRange(range);
    }

    /**
     * Handler for the window range updated signal
     *
     * @param signal
     *            Received signal
     */
    @TmfSignalHandler
    public void windowRangeUpdated(final TmfWindowRangeUpdatedSignal signal) {
        if (signal.getSource() == this) {
            return;
        }
        TmfTimeRange windowRange = signal.getCurrentRange();
        ITraceProject<?, ?> project = fViewContext.getCurrentTraceProject();
        if (windowRange == null || project == null) {
            return;
        }
        fViewContext.setCurrentVisibleTimeRange(fromTmfTimeRange(windowRange));
    }

    // ------------------------------------------------------------------------
    // Util methods
    // ------------------------------------------------------------------------

    /**
     * Convert a {@link TimeRange} range into a {@link TmfTimeRange}.
     *
     * @return The equivalent TmfTimeRange
     */
    private static TmfTimeRange toTmfTimeRange(TimeRange range) {
        return new TmfTimeRange(TmfTimestamp.fromNanos(range.getStartTime()), TmfTimestamp.fromNanos(range.getEndTime()));
    }

    /**
     * Create a {@link TimeRange} from a {@link TmfTimeRange}.
     *
     * @param tmfRange
     *            The TmfTimeRange
     * @return The TimeRange
     */
    private static TimeRange fromTmfTimeRange(TmfTimeRange tmfRange) {
        return TimeRange.of(tmfRange.getStartTime().toNanos(), tmfRange.getEndTime().toNanos());
    }

}
