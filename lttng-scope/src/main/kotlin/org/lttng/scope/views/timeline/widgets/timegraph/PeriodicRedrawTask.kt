/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph

import com.efficios.jabberwocky.context.ViewGroupContext
import org.lttng.scope.views.timeline.TimelineWidget
import java.util.concurrent.atomic.AtomicLong

/**
 * It was quickly determined that having mouse listeners start repaint tasks
 * directly is not a good solution, since simply enqueuing a job can potentially
 * take time, and thus makes scrolling sluggish and annoying to use.
 *
 * Instead, a separate thread can look periodically if the current view's
 * position has changed since its last check, and queue UI updates as needed.
 *
 * This class implements such thread, as a [TimelineWidget.TimelineWidgetUpdateTask].
 */
class PeriodicRedrawTask(private val viewer: TimeGraphWidget) : TimelineWidget.TimelineWidgetUpdateTask {

    /**
     * Sequence number attached to each redraw operation. Can be used for
     * tracing/analysis.
     */
    private val taskSeq = AtomicLong()

    private var previousHorizontalPos = ViewGroupContext.UNINITIALIZED_RANGE
    private var previousVerticalPosition = VerticalPosition.UNINITIALIZED_VP

    @Volatile
    private var forceRedraw = false

    override fun run() {
        if (!viewer.debugOptions.isPaintingEnabled.get()) return

        val currentHorizontalPos = viewer.control.viewContext.visibleTimeRange
        val currentVerticalPos = viewer.currentVerticalPosition

        val movedHorizontally: Boolean
        val movedVertically: Boolean

        if (forceRedraw) {
            forceRedraw = false
            /*
             * Assume the visible window moved, to make sure everything gets
             * repainted on all layers.
             */
            movedHorizontally = true
            movedVertically = true
            /* Then skip the next checks */
        } else {
            movedHorizontally = currentHorizontalPos != previousHorizontalPos
            movedVertically = currentVerticalPos != previousVerticalPosition
            /*
             * Skip painting if the previous position is the exact same as last
             * time. Also skip if were not yet initialized.
             */
            if (!movedHorizontally && !movedVertically) {
                return
            }
            if (currentHorizontalPos == ViewGroupContext.UNINITIALIZED_RANGE
                    || currentVerticalPos == VerticalPosition.UNINITIALIZED_VP) {
                return
            }
        }

        previousHorizontalPos = currentHorizontalPos
        previousVerticalPosition = currentVerticalPos

        viewer.paintArea(currentHorizontalPos, currentVerticalPos,
                movedHorizontally, movedVertically,
                taskSeq.getAndIncrement())
    }

    fun forceRedraw() {
        forceRedraw = true
    }

}
