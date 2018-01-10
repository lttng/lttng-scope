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
import com.efficios.jabberwocky.views.xychart.control.XYChartControl
import com.efficios.jabberwocky.views.xychart.view.XYChartView
import javafx.scene.chart.AreaChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.layout.Region
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartDragHandlers
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartSelectionLayer

abstract class XYChartWidget(override val control: XYChartControl) : XYChartView {

    protected val xAxis = NumberAxis().apply {
        isAutoRanging = false
        isTickMarkVisible = false
        tickUnit = 0.0
    }

    protected val yAxis = NumberAxis().apply {
        isAutoRanging = true
        isTickLabelsVisible = true
    }

    val chart: XYChart<Number, Number> = AreaChart(xAxis, yAxis, null).apply {
        title = null
        isLegendVisible = false
        animated = false
    }

    val chartPlotArea = chart.lookup(".chart-plot-background") as Region

    protected abstract val selectionLayer: XYChartSelectionLayer
    protected abstract val dragHandlers: XYChartDragHandlers

    abstract fun getWidgetTimeRange(): TimeRange

    override fun drawSelection(selectionRange: TimeRange) {
        selectionLayer.drawSelection(selectionRange)
    }


}
