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

import com.efficios.jabberwocky.common.TimeRange;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
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

    /**
     * For now, there is only a single view context for the framework. This
     * method returns this singleton instance.
     *
     * @return The view context
     */
    public static ViewGroupContext getCurrent() {
        ViewGroupContext ctx = INSTANCE;
        if (ctx != null) {
            return ctx;
        }
        ctx = new ViewGroupContext();
        INSTANCE = ctx;
        return ctx;
    }

    /**
     * Cleanup all view group contexts.
     */
    public static void cleanup() {
        ViewGroupContext ctx = INSTANCE;
        if (ctx != null) {
            ctx.fSignalBridge.dispose();
        }
    }

    /**
     * Set the current trace being displayed by this view context.
     *
     * @param trace
     *            The trace, can be null to indicate no trace
     */
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

    /**
     * Retrieve the current trace displayed by this view context.
     *
     * @return The context's current trace. Can be null to indicate no trace.
     */
    public @Nullable ITmfTrace getCurrentTrace() {
        return fCurrentTrace.get();
    }

    /**
     * The current trace property.
     *
     * Make sure you use {@link #setCurrentTrace} to modify the current trace.
     *
     * @return The current trace property.
     */
    public ReadOnlyObjectProperty<@Nullable ITmfTrace> currentTraceProperty() {
        return fCurrentTrace;
    }

    /**
     * Set a new full range for the current trace
     *
     * @param range
     *            The new full range of the trace
     */
    public void setCurrentTraceFullRange(TimeRange range) {
        fCurrentTraceFullRange.set(range);
    }

    /**
     * Get the full range of the current trace.
     *
     * @return The full range of the trace
     */
    public TimeRange getCurrentTraceFullRange() {
        return fCurrentTraceFullRange.get();
    }

    /**
     * The property representing the full range of the current trace.
     *
     * TODO This property should move to the trace object itself.
     *
     * @return The full range property
     */
    public ObjectProperty<TimeRange> currentTraceFullRangeProperty() {
        return fCurrentTraceFullRange;
    }

    /**
     * Set the current visible time range of this view context.
     *
     * @param range
     *            The new visible time range
     */
    public void setCurrentVisibleTimeRange(TimeRange range) {
        fCurrentVisibleTimeRange.set(range);
    }

    /**
     * Retrieve the current visible time range of the view context.
     *
     * @return The current visible time range
     */
    public TimeRange getCurrentVisibleTimeRange() {
        return fCurrentVisibleTimeRange.get();
    }

    /**
     * The visible time range property. This indicates the time range bounded by
     * the views of this context.
     *
     * @return The visible time range property
     */
    public ObjectProperty<TimeRange> currentVisibleTimeRangeProperty() {
        return fCurrentVisibleTimeRange;
    }

    /**
     * Set the current time range selection.
     *
     * @param range
     *            The new selection range
     */
    public void setCurrentSelectionTimeRange(TimeRange range) {
        fCurrentSelectionRange.set(range);
    }

    /**
     * Retrieve the current time range selection
     *
     * @return The current selection range
     */
    public TimeRange getCurrentSelectionTimeRange() {
        return fCurrentSelectionRange.get();
    }

    /**
     * The property representing the current time range selection. This is the
     * "highlighted" part of the trace, which can be selected by the mouse or
     * other means, and on which action can act.
     *
     * @return The time range selection property
     */
    public ObjectProperty<TimeRange> currentSelectionTimeRangeProperty() {
        return fCurrentSelectionRange;
    }

}
