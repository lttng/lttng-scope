/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.context.ViewGroupContext
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import org.lttng.scope.views.context.ViewGroupContextManager
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

object ScopeKeyBindings {

    class VisibleTimeRangeMovingHandler : EventHandler<KeyEvent> {

        companion object {
            private const val SCROLL_STEP = 0.1
            private const val ZOOM_STEP = 0.08
            private const val MINIMUM_VISIBLE_RANGE_DURATION = 1000.0
        }

        override fun handle(e: KeyEvent) {
            /* Only handle this event if the current view context does not have an active project. */
            val viewCtx = ViewGroupContextManager.getCurrent() ?: return
            val project = viewCtx.currentTraceProject ?: return
            val visibleRange = viewCtx.currentVisibleTimeRange

            when (e.code) {
                KeyCode.H, KeyCode.A -> scrollLeft(viewCtx, visibleRange, project.fullRange)
                KeyCode.L, KeyCode.D -> scrollRight(viewCtx, visibleRange, project.fullRange)
                KeyCode.K, KeyCode.W -> zoom(true, viewCtx, visibleRange, project.fullRange)
                KeyCode.J, KeyCode.S -> zoom(false, viewCtx, visibleRange, project.fullRange)
                else -> return
            }
        }

        private fun scrollLeft(viewContext: ViewGroupContext, visibleRange: TimeRange, projectRange: TimeRange) {
            if (visibleRange.startTime <= projectRange.startTime) return

            /* This value is positive, but in time units *towards the left* */
            val offset = (visibleRange.duration * SCROLL_STEP).roundToLong()
            val newStartTime = max(projectRange.startTime, visibleRange.startTime - offset)
            val newEndTime = newStartTime + visibleRange.duration

            viewContext.currentVisibleTimeRange = TimeRange.of(newStartTime, newEndTime)
        }

        private fun scrollRight(viewContext: ViewGroupContext, visibleRange: TimeRange, projectRange: TimeRange) {
            if (visibleRange.endTime >= projectRange.endTime) return

            /* This represents time units towards the right */
            val offset = (visibleRange.duration * SCROLL_STEP).roundToLong()
            val newEndTime = min(projectRange.endTime, visibleRange.endTime + offset)
            val newStartTime = newEndTime - visibleRange.duration

            viewContext.currentVisibleTimeRange = TimeRange.of(newStartTime, newEndTime)
        }

        private fun zoom(zoomIn: Boolean, viewContext: ViewGroupContext, visibleRange: TimeRange, projectRange: TimeRange) {
            val newScaleFactor = if (zoomIn) 1.0 * (1 + ZOOM_STEP) else 1.0 * (1 / (1 + ZOOM_STEP))

            /* "Pivot" here is always the center of visible range */
            val zoomPivot = visibleRange.startTime + (visibleRange.duration / 2)

            val newDuration = max(MINIMUM_VISIBLE_RANGE_DURATION, visibleRange.duration * (1.0 / newScaleFactor))
            val durationDelta = newDuration - visibleRange.duration
            val zoomPivotRatio = (zoomPivot - visibleRange.startTime).toDouble() / visibleRange.duration.toDouble()

            var newStart = visibleRange.startTime - (durationDelta * zoomPivotRatio).roundToLong()
            var newEnd = visibleRange.endTime + (durationDelta - (durationDelta * zoomPivotRatio)).roundToLong()

            /* Clamp newStart and newEnd to the projects's full range */
            newStart = max(newStart, projectRange.startTime)
            newEnd = min(newEnd, projectRange.endTime)

            viewContext.currentVisibleTimeRange = TimeRange.of(newStart, newEnd)
        }
    }

}
