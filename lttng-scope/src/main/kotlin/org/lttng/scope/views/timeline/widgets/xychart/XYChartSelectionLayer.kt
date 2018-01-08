/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.xychart

import com.efficios.jabberwocky.common.TimeRange
import javafx.event.EventHandler
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineCap

abstract class XYChartSelectionLayer(protected val widget: XYChartWidget, protected val chartBackgroundAdjustment: Double) : Pane() {

    companion object {
        /* Style settings. TODO Move to debug options? */
        private const val SELECTION_STROKE_WIDTH = 1.0
        private val SELECTION_STROKE_COLOR = Color.BLUE
        private val SELECTION_FILL_COLOR = Color.LIGHTBLUE.deriveColor(0.0, 1.2, 1.0, 0.4)
    }

    protected val chartPlotArea = widget.chart.lookup(".chart-plot-background") as Region

    protected val selectionRectangle = Rectangle().apply {
        stroke = SELECTION_STROKE_COLOR
        strokeWidth = SELECTION_STROKE_WIDTH
        strokeLineCap = StrokeLineCap.ROUND
    }

    protected val ongoingSelectionRectangle = Rectangle()

    private val selectionCtx = SelectionContext()

    init {
        listOf(selectionRectangle, ongoingSelectionRectangle).forEach {
            // deal
            with(it) {
                isMouseTransparent = true
                fill = SELECTION_FILL_COLOR

                x = 0.0
                width = 0.0
                yProperty().bind(chartPlotArea.layoutYProperty().add(chartBackgroundAdjustment))
                heightProperty().bind(chartPlotArea.heightProperty())

                isVisible = false
            }
        }

        children.addAll(selectionRectangle, ongoingSelectionRectangle)

        /*
         * Add mouse listeners to handle the ongoing selection.
         */
        addEventHandler(MouseEvent.MOUSE_PRESSED, selectionCtx.mousePressedEventHandler)
        addEventHandler(MouseEvent.MOUSE_DRAGGED, selectionCtx.mouseDraggedEventHandler)
        addEventHandler(MouseEvent.MOUSE_RELEASED, selectionCtx.mouseReleasedEventHandler)
    }

    /** Map a x position *inside the chartPlotArea* to its corresponding timestamp. */
    abstract fun mapXPositionToTimestamp(x: Double): Long

    abstract fun drawSelection(sr: TimeRange)

    /**
     * Class encapsulating the time range selection, related drawing and
     * listeners.
     */
    private inner class SelectionContext {

        /**
         * Do not handle the mouse event if it matches these condition. It should be handled
         * at another level (for moving the visible range, etc.)
         */
        private fun MouseEvent.isToBeIgnored(): Boolean =
                this.button == MouseButton.SECONDARY
                        || this.button == MouseButton.MIDDLE
                        || this.isControlDown

        private var ongoingSelection: Boolean = false
        private var mouseOriginX: Double = 0.0

        val mousePressedEventHandler = EventHandler<MouseEvent> { e ->
            if (e.isToBeIgnored()) return@EventHandler
            e.consume()

            if (ongoingSelection) return@EventHandler

            /* Remove the current selection, if there is one */
            selectionRectangle.isVisible = false

            mouseOriginX = e.x

            with(ongoingSelectionRectangle) {
                layoutX = mouseOriginX
                width = 0.0
                isVisible = true
            }

            ongoingSelection = true
        }

        val mouseDraggedEventHandler = EventHandler<MouseEvent> { e ->
            if (e.isToBeIgnored()) return@EventHandler
            e.consume()

            val newX = e.x
            val offsetX = newX - mouseOriginX

            with(ongoingSelectionRectangle) {
                if (offsetX > 0) {
                    layoutX = mouseOriginX
                    width = offsetX
                } else {
                    layoutX = newX
                    width = -offsetX
                }
            }
        }

        val mouseReleasedEventHandler = EventHandler<MouseEvent> { e ->
            if (e.isToBeIgnored()) return@EventHandler
            e.consume()

            ongoingSelectionRectangle.isVisible = false

            /* Send a time range selection signal for the currently highlighted time range */
            val startX = ongoingSelectionRectangle.layoutX
            // FIXME Possible glitch when selecting backwards outside of the window?
            val endX = startX + ongoingSelectionRectangle.width

            val localStartX = chartPlotArea.parentToLocal(startX, 0.0).x - chartBackgroundAdjustment
            val localEndX = chartPlotArea.parentToLocal(endX, 0.0).x - chartBackgroundAdjustment
            val tsStart = mapXPositionToTimestamp(localStartX)
            val tsEnd = mapXPositionToTimestamp(localEndX)

            widget.control.updateTimeRangeSelection(TimeRange.of(tsStart, tsEnd))

            ongoingSelection = false
        }
    }
}

class XYChartFullRangeSelectionLayer(widget: XYChartFullRangeWidget,
                                     chartBackgroundAdjustment: Double) : XYChartSelectionLayer(widget, chartBackgroundAdjustment) {

    override fun mapXPositionToTimestamp(x: Double): Long {
        val project = widget.viewContext.currentTraceProject ?: return 0L

        val viewWidth = chartPlotArea.width
        if (viewWidth < 1.0) return project.startTime

        val posRatio = x / viewWidth
        val ts = (project.startTime + posRatio * project.fullRange.duration).toLong()

        /* Clamp the result to the trace project's range. */
        return ts.clampToRange(project.fullRange)
    }

    override fun drawSelection(sr: TimeRange) {
        val viewWidth = chartPlotArea.width
        if (viewWidth < 1.0) return

        val project = widget.viewContext.currentTraceProject ?: return
        val projectRange = project.fullRange

        val startRatio = (sr.startTime - projectRange.startTime) / projectRange.duration.toDouble()
        val startPos = startRatio * viewWidth + chartPlotArea.layoutX + chartBackgroundAdjustment

        val endRatio = (sr.endTime - projectRange.startTime) / projectRange.duration.toDouble()
        val endPos = endRatio * viewWidth + chartPlotArea.layoutX + chartBackgroundAdjustment

        with(selectionRectangle) {
            x = startPos
            width = endPos - startPos
            isVisible = true
        }
    }

}

class XYChartVisibleRangeSelectionLayer(widget: XYChartVisibleRangeWidget,
                                        chartBackgroundAdjustment: Double) : XYChartSelectionLayer(widget, chartBackgroundAdjustment) {

    override fun mapXPositionToTimestamp(x: Double): Long {
        val vr = widget.viewContext.currentVisibleTimeRange

        val viewWidth = chartPlotArea.width
        if (viewWidth < 1.0) return vr.startTime

        val posRatio = x / viewWidth
        val ts = (vr.startTime + posRatio * vr.duration).toLong()

        /* Clamp the result to the current visible time range. */
        return ts.clampToRange(vr)
    }

    override fun drawSelection(sr: TimeRange) {
        val vr = widget.viewContext.currentVisibleTimeRange

        if (sr.startTime <= vr.startTime && sr.endTime <= vr.startTime) {
            /* Selection is completely before the visible range, no range to display. */
            selectionRectangle.isVisible = false
            return
        }
        if (sr.startTime >= vr.endTime && sr.endTime >= vr.endTime) {
            /* Selection is completely after the visible range, no range to display. */
            selectionRectangle.isVisible = false
            return
        }

        val viewWidth = chartPlotArea.width
        if (viewWidth < 1.0) return

        val startTime = (Math.max(sr.startTime, vr.startTime))
        val startRatio = (startTime - vr.startTime) / vr.duration.toDouble()
        val startPos = startRatio * viewWidth + chartPlotArea.layoutX + chartBackgroundAdjustment

        val endTime = (Math.min(sr.endTime, vr.endTime))
        val endRatio = (endTime - vr.startTime) / vr.duration.toDouble()
        val endPos = endRatio * viewWidth + chartPlotArea.layoutX + chartBackgroundAdjustment

        with(selectionRectangle) {
            x = startPos
            width = endPos - startPos
            isVisible = true
        }
    }
}

private fun Long.clampToRange(range: TimeRange): Long {
    if (this < range.startTime) return range.startTime
    if (this > range.endTime) return range.endTime
    return this
}
