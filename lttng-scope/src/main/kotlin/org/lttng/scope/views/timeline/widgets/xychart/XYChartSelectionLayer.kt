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

    // TODO NYI
    private val ongoingSelectionRectangle = Rectangle()

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
    }

    abstract fun drawSelection(sr: TimeRange)

}

class XYChartFullRangeSelectionLayer(widget: XYChartFullRangeWidget,
                                     chartBackgroundAdjustment: Double) : XYChartSelectionLayer(widget, chartBackgroundAdjustment) {

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