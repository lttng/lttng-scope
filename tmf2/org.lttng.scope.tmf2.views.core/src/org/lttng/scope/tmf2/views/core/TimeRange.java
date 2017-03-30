/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

import com.google.common.base.MoreObjects;

/**
 * Simple class representing a time range, encapsulating two timestamps stored
 * as long's.
 *
 * @author Alexandre Montplaisir
 */
public final class TimeRange {

    private final long fStartTime;
    private final long fEndTime;

    private TimeRange(long startTime, long endTime) {
        if (endTime < startTime) {
            throw new IllegalArgumentException("End of time range earlier than the start"); //$NON-NLS-1$
        }
        if (startTime < 0 || endTime < 0) {
            throw new IllegalArgumentException("One of the bounds is negative"); //$NON-NLS-1$
        }
        fStartTime = startTime;
        fEndTime = endTime;
    }

    /**
     * Factory method, creating a new time range from its start and end.
     *
     * @param startTime
     *            The start time of the range
     * @param endTime
     *            The end time of the range. Should be equal or greater than the
     *            start time.
     * @return The new time range
     */
    public static TimeRange of(long startTime, long endTime) {
        // TODO Implement caching?
        return new TimeRange(startTime, endTime);
    }

    /**
     * Get the range's start time
     *
     * @return The start time
     */
    public long getStart() {
        return fStartTime;
    }

    /**
     * Get the range's end time
     *
     * @return The end time
     */
    public long getEnd() {
        return fEndTime;
    }

    /**
     * Get the duration, or "span", of this time range.
     *
     * @return The duration
     */
    public long getDuration() {
        return (fEndTime - fStartTime);
    }

    /**
     * Check if this time range contains, inclusively, the given timestamp.
     *
     * @param timestamp
     *            The timestamp to check
     * @return True if the timestamp is contained in the range, false otherwise
     */
    public boolean contains(long timestamp) {
        return (fEndTime <= timestamp && timestamp <= fStartTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fStartTime, fEndTime);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TimeRange other = (TimeRange) obj;
        return (fStartTime == other.fStartTime
                && fEndTime == other.fEndTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("start", fStartTime) //$NON-NLS-1$
                .add("end", fEndTime) //$NON-NLS-1$
                .toString();
    }

    /**
     * Convert this range into a {@link TmfTimeRange}.
     *
     * @return The equivalent TmfTimeRange
     */
    public TmfTimeRange toTmfTimeRange() {
        return new TmfTimeRange(TmfTimestamp.fromNanos(fStartTime), TmfTimestamp.fromNanos(fEndTime));
    }

    /**
     * Create a {@link TimeRange} from a {@link TmfTimeRange}.
     *
     * @param range
     *            The TmfTimeRange
     * @return The TimeRange
     */
    public static TimeRange fromTmfTimeRange(TmfTimeRange range) {
        return new TimeRange(range.getStartTime().toNanos(), range.getEndTime().toNanos());
    }

}
