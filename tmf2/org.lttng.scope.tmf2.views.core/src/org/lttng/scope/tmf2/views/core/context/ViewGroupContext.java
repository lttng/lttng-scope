/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.context;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.TimeRange;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A common context for a group of views. Information is stored as properties,
 * and views can add listeners to get notified of value changes.
 *
 * @author Alexandre Montplaisir
 */
public class ViewGroupContext {

    /** Value representing uninitialized timestamps */
    public static final TimeRange UNINITIALIZED_RANGE = TimeRange.of(0, 0);

    private final SignalBridge fSignalBridge;

    private final ObjectProperty<@Nullable ITmfTrace> fCurrentTrace = new SimpleObjectProperty<>(null);

    private ObjectProperty<TimeRange> fCurrentTraceFullRange = new SimpleObjectProperty<>(UNINITIALIZED_RANGE);
    private ObjectProperty<TimeRange> fCurrentVisibleTimeRange = new SimpleObjectProperty<>(UNINITIALIZED_RANGE);
    private ObjectProperty<TimeRange> fCurrentSelectionRange = new SimpleObjectProperty<>(UNINITIALIZED_RANGE);

    /**
     * The context is a singleton for now, but the framework could be extended
     * to support several contexts (one per "view group") at the same time.
     */
    private static @Nullable ViewGroupContext INSTANCE;

    private ViewGroupContext() {
        fSignalBridge = new SignalBridge(this);
    }

    public static ViewGroupContext getCurrent() {
        ViewGroupContext ctx = INSTANCE;
        if (ctx != null) {
            return ctx;
        }
        ctx = new ViewGroupContext();
        INSTANCE = ctx;
        return ctx;
    }

    public static void cleanup() {
        ViewGroupContext ctx = INSTANCE;
        if (ctx != null) {
            ctx.fSignalBridge.dispose();
        }
    }

    public void setCurrentTrace(@Nullable ITmfTrace trace) {
        /* On trace change, adjust the other properties accordingly. */
        if (trace == null) {
            fCurrentTraceFullRange = new SimpleObjectProperty<>(UNINITIALIZED_RANGE);
            fCurrentVisibleTimeRange = new SimpleObjectProperty<>(UNINITIALIZED_RANGE);
            fCurrentSelectionRange = new SimpleObjectProperty<>(UNINITIALIZED_RANGE);

        } else {
            long traceStart = trace.getStartTime().toNanos();
            long traceEnd = trace.getEndTime().toNanos();
            long visibleRangeEnd = Math.min(traceStart + trace.getInitialRangeOffset().toNanos(), traceEnd);

            fCurrentTraceFullRange = new SimpleObjectProperty<>((TimeRange.of(traceStart, traceEnd)));
            fCurrentVisibleTimeRange = new SimpleObjectProperty<>((TimeRange.of(traceStart, visibleRangeEnd)));
            fCurrentSelectionRange = new SimpleObjectProperty<>((TimeRange.of(traceStart, traceStart)));
        }

        fCurrentTrace.set(trace);
    }

    public @Nullable ITmfTrace getCurrentTrace() {
        return fCurrentTrace.get();
    }

    public ObjectProperty<@Nullable ITmfTrace> currentTraceProperty() {
        return fCurrentTrace;
    }

    public void setCurrentTraceFullRange(TimeRange range) {
        fCurrentTraceFullRange.set(range);
    }

    public TimeRange getCurrentTraceFullRange() {
        return fCurrentTraceFullRange.get();
    }

    public ObjectProperty<TimeRange> currentTraceFullRangeProperty() {
        return fCurrentTraceFullRange;
    }

    public void setCurrentVisibleTimeRange(TimeRange range) {
        fCurrentVisibleTimeRange.set(range);
    }

    public TimeRange getCurrentVisibleTimeRange() {
        return fCurrentVisibleTimeRange.get();
    }

    public ObjectProperty<TimeRange> currentVisibleTimeRangeProperty() {
        return fCurrentVisibleTimeRange;
    }

    public void setCurrentSelectionTimeRange(TimeRange range) {
        fCurrentSelectionRange.set(range);
    }

    public TimeRange getCurrentSelectionTimeRange() {
        return fCurrentSelectionRange.get();
    }

    public ObjectProperty<TimeRange> currentSelectionTimeRangeProperty() {
        return fCurrentSelectionRange;
    }

}
