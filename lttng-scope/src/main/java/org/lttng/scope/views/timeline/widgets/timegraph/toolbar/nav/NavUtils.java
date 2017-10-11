/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar.nav;

import com.efficios.jabberwocky.common.TimeRange;
import org.lttng.scope.views.context.ViewGroupContextManager;
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget;

/**
 * Common utilities for navigation actions.
 *
 * @author Alexandre Montplaisir
 */
final class NavUtils {

    private NavUtils() {}

    /**
     * Move the selection to the target timestamp. Also update the visible range
     * to be centered on that timestamp, but only if it is outside of the
     * current visible range.
     *
     * This should only be called when reaching the new timestamp is caused by a
     * user action (ie, not simply because another view sent a signal).
     *
     * @param viewer
     *            The viewer on which to work
     * @param timestamp
     *            The timestamp to select, and potentially move to
     */
    public static void selectNewTimestamp(TimeGraphWidget viewer, long timestamp) {
        /* Update the selection to the new timestamp. */
        viewer.getControl().updateTimeRangeSelection(TimeRange.of(timestamp, timestamp));

        TimeRange fullTimeGraphRange = viewer.getControl().getViewContext().getCurrentProjectFullRange();
        TimeRange windowRange = ViewGroupContextManager.getCurrent().getCurrentVisibleTimeRange();
        long windowStart = windowRange.getStartTime();
        long windowEnd = windowRange.getEndTime();
        if (windowStart <= timestamp && timestamp <= windowEnd) {
            /* Timestamp is still in the visible range, don't touch anything. */
            return;
        }
        /* Update the visible range to the requested timestamp. */
        /* The "span" of the window (aka zoom level) will remain constant. */
        long windowSpan = windowEnd - windowStart;
        if (windowSpan > fullTimeGraphRange.getDuration()) {
            /* Should never happen, but just to be mathematically safe. */
            windowSpan = fullTimeGraphRange.getDuration();
        }

        long newStart = timestamp - (windowSpan / 2);
        long newEnd = newStart + windowSpan;

        /* Clamp the range to the borders of the pane/trace. */
        if (newStart < fullTimeGraphRange.getStartTime()) {
            newStart = fullTimeGraphRange.getStartTime();
            newEnd = newStart + windowSpan;
        } else if (newEnd > fullTimeGraphRange.getEndTime()) {
            newEnd = fullTimeGraphRange.getEndTime();
            newStart = newEnd - windowSpan;
        }

        viewer.getControl().updateVisibleTimeRange(TimeRange.of(newStart, newEnd), true);
    }
}
