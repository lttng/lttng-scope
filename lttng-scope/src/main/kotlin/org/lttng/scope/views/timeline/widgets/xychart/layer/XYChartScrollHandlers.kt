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
import javafx.scene.input.ScrollEvent
import org.lttng.scope.views.timeline.widgets.xychart.XYChartVisibleRangeWidget
import org.lttng.scope.views.timeline.widgets.xychart.XYChartWidget
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

class XYChartScrollHandlers(private val widget: XYChartWidget) {

    companion object {
        private const val ZOOM_STEP = 0.08
        /** Minimum allowed zoom level, in nanos per pixel  */
        private const val ZOOM_LIMIT = 1.0
    }

    private val mouseScrollHandler = EventHandler<ScrollEvent> { e ->
        if (!e.isControlDown) return@EventHandler
        e.consume();

        val forceUseMousePosition = e.isShiftDown

        val delta = e.deltaY
        val zoomIn: Boolean = (delta > 0.0); // false means a zoom-out

        /*
         * getX() corresponds to the X position of the mouse on the time graph.
         * This is seriously awesome.
         */
        zoom(zoomIn, forceUseMousePosition, e.getX());
    }

    init {
        widget.chart.onScroll = mouseScrollHandler
    }

    private fun zoom(zoomIn: Boolean, forceUseMousePosition: Boolean, mouseX: Double?) {
        val newScaleFactor = if (zoomIn) 1.0 * (1 + ZOOM_STEP) else 1.0 * (1 / (1 + ZOOM_STEP))

        /* Send a corresponding window-range signal to the control */
        val control = widget.control
        val visibleRange = widget.viewContext.currentVisibleTimeRange
        val currentSelection = widget.viewContext.currentSelectionTimeRange
        val currentSelectionCenter = (currentSelection.duration / 2) + currentSelection.startTime
        val currentSelectionCenterIsVisible = visibleRange.contains(currentSelectionCenter)

        // TODO zoomPivot location is not perfectly precise (something to do with chartBackgroundAdjustment probably)
        // It's minor enough that we can live with for now, pivot position is not extremely important.

        val zoomPivot = if (mouseX != null && forceUseMousePosition) {
            /* Pivot on mouse position */
            widget.mapXPositionToTimestamp(mouseX)
        } else if (currentSelectionCenterIsVisible) {
            /* Pivot on current selection center */
            currentSelectionCenter
        } else if (mouseX != null && mouseIsInVisibleRange(mouseX)) {
            /* Pivot on mouse position */
            widget.mapXPositionToTimestamp(mouseX)
        } else {
            /* Pivot on center of visible range */
            visibleRange.startTime + (visibleRange.duration / 2)
        }

        /* Prevent going closer than the zoom limit */
        val timeGraphVisibleWidth = max(1.0, widget.chart.width)
        val minDuration = ZOOM_LIMIT * timeGraphVisibleWidth

        val newDuration = max(minDuration, visibleRange.duration * (1.0 / newScaleFactor))
        val durationDelta = newDuration - visibleRange.duration
        val zoomPivotRatio = (zoomPivot - visibleRange.startTime).toDouble() / visibleRange.duration.toDouble()

        var newStart = visibleRange.startTime - (durationDelta * zoomPivotRatio).roundToLong()
        var newEnd = visibleRange.endTime + (durationDelta - (durationDelta * zoomPivotRatio)).roundToLong()

        /* Clamp newStart and newEnd to the full trace's range */
        val fullRange = control.viewContext.getCurrentProjectFullRange()
        newStart = max(newStart, fullRange.startTime)
        newEnd = min(newEnd, fullRange.endTime)

        control.updateVisibleTimeRange(TimeRange.of(newStart, newEnd), true);
    }

    private fun mouseIsInVisibleRange(mouseX: Double): Boolean {
        if (widget is XYChartVisibleRangeWidget) return true
        val visibleRange = widget.viewContext.currentVisibleTimeRange
        val mouseTs = widget.mapXPositionToTimestamp(mouseX)
        return visibleRange.contains(mouseTs)
    }
}