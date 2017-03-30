/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.control;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.lttng.scope.common.core.log.TraceCompassLog;
import org.lttng.scope.tmf2.views.core.TimeRange;

import com.google.common.annotations.VisibleForTesting;

// TODO Needs to be public only because of TmfSignalManager
public class SignallingContext {

    private static final Logger LOGGER = TraceCompassLog.getLogger(SignallingContext.class);

    /**
     * Executor to execute signal handlers in separate threads, to ensure they
     * are not run on the UI thread.
     *
     * TODO This should really be the signal manager's responsibility!
     */
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static final int SIGNAL_DELAY_MS = 200;

    private final TmfSignalThrottler fVisibleRangeSignalThrottler = new TmfSignalThrottler(null, SIGNAL_DELAY_MS);

    private final TimeGraphModelControl fControl;

    public SignallingContext(TimeGraphModelControl control) {
        TmfSignalManager.register(this);
        fControl = control;
    }

    public void dispose() {
        TmfSignalManager.deregister(this);
    }

    public void sendTimeRangeSelectionUpdate(TimeRange selectionRange) {
        TmfSignal signal = new TmfSelectionRangeUpdatedSignal(this,
                TmfTimestamp.fromNanos(selectionRange.getStart()),
                TmfTimestamp.fromNanos(selectionRange.getEnd()));
        TmfSignalManager.dispatchSignal(signal);
    }

    public void sendVisibleWindowRangeUpdate(TimeRange windowRange, boolean echo) {
        TmfSignal signal = new TmfWindowRangeUpdatedSignal(this,
                windowRange.toTmfTimeRange(),
                echo);
        fVisibleRangeSignalThrottler.queue(signal);
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        EXECUTOR.execute(() -> {
            LOGGER.fine(() -> "[SignallingContext:ReceivedTraceOpen] trace=" + signal.getTrace().toString()); //$NON-NLS-1$

            ITmfTrace newTrace = signal.getTrace();
            fControl.initializeForTrace(newTrace);
        });
    }

    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        EXECUTOR.execute(() -> {
            LOGGER.fine(() -> "[SignallingContext:ReceivedTraceSelected] trace=" + signal.getTrace().toString());

            ITmfTrace newTrace = signal.getTrace();
            fControl.initializeForTrace(newTrace);
        });
    }

    /**
     * @param signal
     */
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
        EXECUTOR.execute(() -> {
            LOGGER.fine(() -> "[SignallingContext:ReceivedTraceClosed] " + //$NON-NLS-1$
                    signal.getTrace().getName());

            /*
             * Clear the view, but only if there is no other active trace. This
             * check should really be the signal manager's job...
             */
            if (TmfTraceManager.getInstance().getActiveTrace() == null) {
                fControl.initializeForTrace(null);
            }
        });
    }

    /**
     * @param signal
     */
    @TmfSignalHandler
    public void traceRangeUpdated(TmfTraceRangeUpdatedSignal signal) {
        EXECUTOR.execute(() -> {
            long rangeStart = signal.getRange().getStartTime().toNanos();
            long rangeEnd = signal.getRange().getEndTime().toNanos();

            LOGGER.finer(() -> "[SignallingContext:ReceivedTraceRangeUpdated] " + //$NON-NLS-1$
                    String.format("rangeStart=%,d, rangeEnd=%,d", //$NON-NLS-1$
                            rangeStart, rangeEnd));
            /*
             * This signal is a disaster, it's very inconsistent, has no
             * guarantee of even showing up and sometimes gives values outside
             * of the trace's own range. Best to ignore its contents completely.
             */

            ITmfTrace trace = fControl.getCurrentTrace();
            if (trace != null) {
                long traceStart = trace.getStartTime().toNanos();
                long traceEnd = trace.getEndTime().toNanos();
                fControl.setFullTimeRange(TimeRange.of(traceStart, traceEnd));
            }
        });
    }

    /**
     * @param signal
     */
    @TmfSignalHandler
    public void selectionRangeUpdated(final TmfSelectionRangeUpdatedSignal signal) {
        EXECUTOR.execute(() -> {
            long rangeStart = signal.getBeginTime().toNanos();
            long rangeEnd = signal.getEndTime().toNanos();

            LOGGER.finer(() -> "[SignallingContext:ReceivedSelectionRangeUpdated] " + //$NON-NLS-1$
                    String.format("rangeStart=%,d, rangeEnd=%,d", //$NON-NLS-1$
                            rangeStart, rangeEnd));

            if (rangeStart > rangeEnd) {
                /*
                 * This signal's end can be before its start time, against all
                 * logic
                 */
                fControl.drawSelection(TimeRange.of(rangeEnd, rangeStart));
            } else {
                fControl.drawSelection(TimeRange.of(rangeStart, rangeEnd));
            }
        });
    }

    /**
     * @param signal
     */
    @TmfSignalHandler
    public void windowRangeUpdated(final TmfWindowRangeUpdatedSignal signal) {
        EXECUTOR.execute(() -> {
            ITmfTrace trace = fControl.getCurrentTrace();
            if (trace == null) {
                return;
            }

            long traceStart = trace.getStartTime().toNanos();
            long traceEnd = trace.getEndTime().toNanos();

            TmfTimeRange windowRange = signal.getCurrentRange();

            LOGGER.finer(() -> "[SignallingContext:ReceivedWindowRangeUpdated] " + //$NON-NLS-1$
                    windowRange.toString());

            fControl.setFullTimeRange(TimeRange.of(traceStart, traceEnd));

            if (signal.getSource() != this || signal.echo()) {
                /*
                 * If the signal came from the time graph's own scrollbar, then
                 * the zoom level did not change, and the view is already at the
                 * position we want it.
                 *
                 * Zoom events for instance will set "echo" to true, which means
                 * the view wants to receive the signal back.
                 */
                fControl.seekVisibleRange(TimeRange.fromTmfTimeRange(windowRange));
            } else {
                fControl.setVisibleTimeRange(TimeRange.fromTmfTimeRange(windowRange));
            }

            if (fCurrentSignalLatch != null) {
                fCurrentSignalLatch.countDown();
            }
        });
    }

    // ------------------------------------------------------------------------
    // Test utilities
    // ------------------------------------------------------------------------

    private volatile @Nullable CountDownLatch fCurrentSignalLatch = null;

    @VisibleForTesting
    void prepareWaitForNextSignal() {
        if (fCurrentSignalLatch != null) {
            throw new IllegalStateException("Do not call this method concurrently!"); //$NON-NLS-1$
        }
        fCurrentSignalLatch = new CountDownLatch(1);
    }

    @VisibleForTesting
    void waitForNextSignal() {
        CountDownLatch latch = fCurrentSignalLatch;
        if (latch == null) {
            throw new IllegalStateException("Do not call this method concurrently!"); //$NON-NLS-1$
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
        fCurrentSignalLatch = null;
    }
}
