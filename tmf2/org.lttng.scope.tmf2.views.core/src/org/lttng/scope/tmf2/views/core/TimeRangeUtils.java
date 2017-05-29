/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core;

import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

import com.efficios.jabberwocky.common.TimeRange;

/**
 * Bridge between Jabberwocky and TMF Time Range classes.
 *
 * @author Alexandre Montplaisir
 */
public final class TimeRangeUtils {

    private TimeRangeUtils() {}

    /**
     * Convert a {@link TimeRange} range into a {@link TmfTimeRange}.
     *
     * @return The equivalent TmfTimeRange
     */
    public static TmfTimeRange toTmfTimeRange(TimeRange range) {
        return new TmfTimeRange(TmfTimestamp.fromNanos(range.getStartTime()), TmfTimestamp.fromNanos(range.getEndTime()));
    }

    /**
     * Create a {@link TimeRange} from a {@link TmfTimeRange}.
     *
     * @param tmfRange
     *            The TmfTimeRange
     * @return The TimeRange
     */
    public static TimeRange fromTmfTimeRange(TmfTimeRange tmfRange) {
        return TimeRange.of(tmfRange.getStartTime().toNanos(), tmfRange.getEndTime().toNanos());
    }

}
