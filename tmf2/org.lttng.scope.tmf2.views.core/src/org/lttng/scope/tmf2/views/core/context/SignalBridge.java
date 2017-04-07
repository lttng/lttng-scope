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

// TODO Needs to be public because of TmfSignalManager
public class SignalBridge {

    private static final int SIGNAL_DELAY_MS = 200;

    private final TmfSignalThrottler fVisibleRangeSignalThrottler = new TmfSignalThrottler(null, SIGNAL_DELAY_MS);

    private final ViewGroupContext fViewContext;


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

    public void dispose() {
        TmfSignalManager.deregister(this);
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        if (signal.getSource() == this) {
            return;
        }
        ITmfTrace trace = signal.getTrace();
        fViewContext.setCurrentTrace(trace);
    }

    /**
     * @param signal
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
     * @param signal
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
     * @param signal
     */
    @TmfSignalHandler
    public void selectionRangeUpdated(final TmfSelectionRangeUpdatedSignal signal) {
        if (signal.getSource() == this) {
            return;
        }
        long rangeStart = signal.getBeginTime().toNanos();
        long rangeEnd = signal.getEndTime().toNanos();

        TimeRange range = (rangeStart > rangeEnd) ?
        /*
         * This signal's end can be before its start time, against all logic
         */
                TimeRange.of(rangeEnd, rangeStart) : TimeRange.of(rangeStart, rangeEnd);

        ITmfTrace trace = fViewContext.getCurrentTrace();
        if (trace == null) {
            return;
        }
        fViewContext.setCurrentSelectionTimeRange(range);
    }

    /**
     * @param signal
     */
    @TmfSignalHandler
    public void windowRangeUpdated(final TmfWindowRangeUpdatedSignal signal) {
        if (signal.getSource() == this) {
            return;
        }
        TmfTimeRange windowRange = signal.getCurrentRange();

        ITmfTrace trace = fViewContext.getCurrentTrace();
        if (trace == null) {
            return;
        }
        fViewContext.setCurrentVisibleTimeRange(TimeRange.fromTmfTimeRange(windowRange));
    }

}
