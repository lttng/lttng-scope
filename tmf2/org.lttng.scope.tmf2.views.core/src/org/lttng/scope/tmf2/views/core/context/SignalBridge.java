/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.context;

import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.lttng.scope.tmf2.views.core.TimeRange;

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

        viewContext.currentTraceProperty().addListener((obs, oldTrace, newTrace) -> {
            if (newTrace == null) {
                return;
            }
            TmfSignal newTraceSignal = new TmfTraceSelectedSignal(SignalBridge.this, newTrace);
            TmfSignalManager.dispatchSignal(newTraceSignal);

            viewContext.currentTraceFullRangeProperty().addListener((observable, oldRange, newRange) -> {
                TmfTimeRange tmfTimeRange = newRange.toTmfTimeRange();
                TmfSignal signal = new TmfTraceRangeUpdatedSignal(SignalBridge.this, viewContext.getCurrentTrace(), tmfTimeRange);
                TmfSignalManager.dispatchSignal(signal);
            });

            viewContext.currentVisibleTimeRangeProperty().addListener((observable, oldRange, newRange) -> {
                TmfTimeRange tmfTimeRange = newRange.toTmfTimeRange();
                TmfSignal signal = new TmfWindowRangeUpdatedSignal(SignalBridge.this, tmfTimeRange);
                fVisibleRangeSignalThrottler.queue(signal);
            });

            viewContext.currentSelectionTimeRangeProperty().addListener((observable, oldRange, newRange) -> {
                TmfSignal signal;
                if (newRange.isSingleTimestamp()) {
                    ITmfTimestamp ts = TmfTimestamp.fromNanos(newRange.getStart());
                    signal = new TmfSelectionRangeUpdatedSignal(SignalBridge.this, ts);
                } else {
                    ITmfTimestamp startTs = TmfTimestamp.fromNanos(newRange.getStart());
                    ITmfTimestamp endTs = TmfTimestamp.fromNanos(newRange.getEnd());
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
        ITmfTrace trace = signal.getTrace();
        fViewContext.setCurrentTrace(trace);
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
        if (TmfTraceManager.getInstance().getActiveTrace() == null) {
            fViewContext.setCurrentTrace(null);
        }
    }

    /**
     * Handler for the trace range updated signal
     *
     * @param signal
     *            Received signal
     */
    @TmfSignalHandler
    public void traceRangeUpdated(TmfTraceRangeUpdatedSignal signal) {
        if (signal.getSource() == this) {
            return;
        }
        /*
         * This signal is a disaster, it's very inconsistent, has no guarantee
         * of even showing up and sometimes gives values outside of the trace's
         * own range. Best to ignore its contents completely.
         */

        ITmfTrace trace = fViewContext.getCurrentTrace();
        if (trace == null) {
            return;
        }
        long traceStart = trace.getStartTime().toNanos();
        long traceEnd = trace.getEndTime().toNanos();
        fViewContext.setCurrentTraceFullRange(TimeRange.of(traceStart, traceEnd));
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

        ITmfTrace trace = fViewContext.getCurrentTrace();
        if (trace == null) {
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
        ITmfTrace trace = fViewContext.getCurrentTrace();
        if (windowRange == null || trace == null) {
            return;
        }
        fViewContext.setCurrentVisibleTimeRange(TimeRange.fromTmfTimeRange(windowRange));
    }

}
