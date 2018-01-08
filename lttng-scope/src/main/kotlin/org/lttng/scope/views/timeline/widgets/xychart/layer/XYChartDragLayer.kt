/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.xychart.layer

import com.efficios.jabberwocky.common.TimeRange
import javafx.event.EventHandler
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import org.lttng.scope.views.timeline.widgets.xychart.XYChartVisibleRangeWidget
import org.lttng.scope.views.timeline.widgets.xychart.XYChartWidget
import kotlin.math.roundToLong

class XYChartDragLayer(private val widget: XYChartWidget) {

    private val dragCtx = DragContext()

    init {
        /*
         * Add mouse listeners to handle mouse dragging operations.
         */
        with(widget.chart) {
            addEventHandler(MouseEvent.MOUSE_PRESSED, dragCtx.mousePressedEventHandler)
            addEventHandler(MouseEvent.MOUSE_DRAGGED, dragCtx.mouseDraggedEventHandler)
            addEventHandler(MouseEvent.MOUSE_RELEASED, dragCtx.mouseReleasedEventHandler)
        }
    }

    private inner class DragContext {

        private var ongoingDrag = false
        private var lastPos = 0.0

        private fun MouseEvent.needsHandling(): Boolean =
                this.button == MouseButton.MIDDLE || this.button == MouseButton.SECONDARY ||  this.isControlDown

        val mousePressedEventHandler = EventHandler<MouseEvent> { e ->
            if (!e.needsHandling()) return@EventHandler
            e.consume()

            if (ongoingDrag) return@EventHandler

            lastPos = e.x
            ongoingDrag = true
        }

        val mouseDraggedEventHandler = EventHandler<MouseEvent> { e ->
            if (!e.needsHandling()) return@EventHandler
            e.consume()

            val newX = e.x
            val offsetX = newX - lastPos
            lastPos = newX

            /* Determine the "offset" represents which time delta. */
            val fullWidth = widget.chartPlotArea.width
            val offsetRatio = offsetX / fullWidth
            if (offsetRatio.isNaN()) return@EventHandler
            var offsetTimeDelta: Long = (offsetRatio * widget.getWidgetTimeRange().duration).roundToLong()

            /* Slide the visible time range accordingly */
            val visibleRange = widget.viewContext.currentVisibleTimeRange
            val projectRange = widget.viewContext.getCurrentProjectFullRange()
            if (offsetTimeDelta == 0L) return@EventHandler

            /*
             * Swap the drag direction for the visible-range widget, so that it uses
             * "natural" scrolling like the timegraphs do.
             */
            if (widget is XYChartVisibleRangeWidget) {
                offsetTimeDelta = -offsetTimeDelta
            }

            val newVisibleRange: TimeRange = if (offsetTimeDelta > 0) {
                /* We're dragging towards the end of the trace. */
                if (visibleRange.endTime == projectRange.endTime) return@EventHandler

                if (visibleRange.endTime + offsetTimeDelta >= projectRange.endTime) {
                    /* We're overshooting, just cap the visible range to the end of the trace. */
                    val newEndTime = projectRange.endTime
                    val newStartTime = newEndTime - visibleRange.duration
                    TimeRange.of(newStartTime, newEndTime)
                } else {
                    /* There is room to slide smoothly. */
                    TimeRange.of(visibleRange.startTime + offsetTimeDelta, visibleRange.endTime + offsetTimeDelta)
                }

            } else {
                /* We're dragging towards the beginning of the trace. */
                if (visibleRange.startTime == projectRange.startTime) return@EventHandler

                // The '+' is wanted here, 'offsetTimeDelta' is negative. */
                if (visibleRange.startTime + offsetTimeDelta <= projectRange.startTime) {
                    /* We're overshooting, cap the visible range to the trace's start. */
                    val newStartTime = projectRange.startTime
                    val newEndTime = newStartTime + visibleRange.duration
                    TimeRange.of(newStartTime, newEndTime)
                } else {
                    /* There is enough room to slide the visible range. */
                    // Again, '+' because 'offsetTimeDelta' is negative.
                    TimeRange.of(visibleRange.startTime + offsetTimeDelta, visibleRange.endTime + offsetTimeDelta)
                }
            }

            widget.control.updateVisibleTimeRange(newVisibleRange, true)
        }

        val mouseReleasedEventHandler = EventHandler<MouseEvent> { e ->
            if (!e.needsHandling()) return@EventHandler
            e.consume()
            ongoingDrag = false
        }

    }

}
