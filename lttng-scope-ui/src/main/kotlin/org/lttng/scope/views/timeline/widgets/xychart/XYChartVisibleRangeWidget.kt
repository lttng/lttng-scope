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
import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.views.xychart.control.XYChartControl
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.chart.XYChart
import javafx.scene.control.Label
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.lttng.scope.application.ScopeOptions
import org.lttng.scope.common.jfx.TimeAxis
import org.lttng.scope.views.timeline.TimelineWidget
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartDragHandlers
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartScrollHandlers
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartSelectionLayer

/**
 * Widget for the timeline view showing data in a XY-Chart. The contents of the
 * chart will follow the framework's current visible range and update its display
 * accordingly.
 */
class XYChartVisibleRangeWidget(control: XYChartControl, override val weight: Int) : XYChartWidget(control), TimelineWidget {

    companion object {
        /* Number of data points we request to the backend */
        private const val NB_DATA_POINTS = 120
    }

    override val name = control.renderProvider.providerName
    override val rootNode: Parent
    override val splitPane: SplitPane

    private val chartArea: Pane

    override val selectionLayer = XYChartSelectionLayer.build(this, 5.0)
    override val dragHandlers = XYChartDragHandlers(this)
    override val scrollHandlers = XYChartScrollHandlers(this)

    private val timestampFormatChangeListener = InvalidationListener {
        Platform.runLater() {
        }
    }

    init {
        var infoArea : BorderPane? = null
        val lblName = Label(name)

        lblName.style = "-fx-font-weight: bold"

        val vBox = VBox()

        vBox.alignment = Pos.CENTER
        vBox.children.add(lblName)
        vBox.children.add(Label("(Count / Time)"))
        infoArea = BorderPane(vBox)
        chartArea = StackPane(chart, selectionLayer)
        splitPane = SplitPane(infoArea, chartArea)
        rootNode = BorderPane(splitPane)

        with(xAxis) {
            isTickMarkVisible = true
            isTickLabelsVisible = true
        }

        ScopeOptions.timestampFormatProperty().addListener(timestampFormatChangeListener)
        ScopeOptions.timestampTimeZoneProperty().addListener(timestampFormatChangeListener)
    }

    override fun dispose() {
        ScopeOptions.timestampFormatProperty().removeListener(timestampFormatChangeListener)
        ScopeOptions.timestampTimeZoneProperty().removeListener(timestampFormatChangeListener)
    }

    override fun getWidgetTimeRange() = viewContext.visibleTimeRange

    override fun mapXPositionToTimestamp(x: Double): Long {
        val vr = viewContext.visibleTimeRange

        val viewWidth = chartPlotArea.width
        if (viewWidth < 1.0) return vr.startTime

        val posRatio = x / viewWidth
        val ts = (vr.startTime + posRatio * vr.duration).toLong()

        /* Clamp the result to the current visible time range. */
        return ts.coerceIn(vr.startTime, vr.endTime)
    }

    // ------------------------------------------------------------------------
    // TimelineWidget
    // ------------------------------------------------------------------------

    override val timelineWidgetUpdateTask: TimelineWidget.TimelineWidgetUpdateTask = RedrawTask()

    /**
     * Even though the chart updates its data according to the time range, it is not
     * done by using a scroll pane.
     */
    override val timeBasedScrollPane = null

    // TODO Bind the selection rectangles with the other timeline ones?
    override val selectionRectangle = null
    override val ongoingSelectionRectangle = null

    // ------------------------------------------------------------------------
    // XYChartView
    // ------------------------------------------------------------------------

    override fun clear() {
        /* Nothing to do, the redraw task will remove all series if the trace is null. */
    }

    override fun seekVisibleRange(newVisibleRange: TimeRange) {
        /*
         * Nothing special to do regarding the data, the redraw task will repopulate
         * the charts with the correct data.
         *
         * However we need to redraw the selection rectangle since it probably moved.
         */
        drawSelection(viewContext.selectionTimeRange)
    }

    private inner class RedrawTask : TimelineWidget.TimelineWidgetUpdateTask {

        private var previousVisibleRange = ViewGroupContext.UNINITIALIZED_RANGE

        override fun run() {
            /* Skip redraws if we are in a project-switching operation. */
            if (viewContext.listenerFreeze) return

            val newVisibleRange = viewContext.visibleTimeRange
            if (newVisibleRange == previousVisibleRange) return

            /* Paint a new chart */
            val renders = control.renderProvider.generateSeriesRenders(newVisibleRange, NB_DATA_POINTS, null)
            if (renders.isEmpty()) return

            val outSeries = mutableListOf<XYChart.Series<Number, Number>>()

            for (render: XYChartRender in renders) {
                val outPoints = mutableListOf<XYChart.Data<Number, Number>>()

                // Width of a single bar in nanoseconds
                val step = (render.data[1].x - render.data[0].x)

                /*
                 * A data point indicates the count difference between the data
                 * points's timestamp and the next data point's timestamp.
                 *
                 * Hack: simulate a bar chart with an area chart here.
                 */
                for (renderDp: XYChartRender.DataPoint in render.data) {
                    outPoints.add(XYChart.Data(renderDp.x, 0.0))
                    outPoints.add(XYChart.Data(renderDp.x, renderDp.y))
                    outPoints.add(XYChart.Data(renderDp.x + step, renderDp.y))
                    outPoints.add(XYChart.Data(renderDp.x + step, 0.0))
                }

                outSeries.add(XYChart.Series(render.series.name, FXCollections.observableList(outPoints)))
            }

            /* Determine start and end times of the display range. */
            val start = renders.map { it.range.startTime }.min()!!
            val end = renders.map { it.range.endTime }.max()!!
            val range = TimeRange.of(start, end)
            val tickValue = renders.first().resolutionX * 10

            xAxis.setTickStep(renders.first().resolutionX)

            Platform.runLater {
                chart.data = FXCollections.observableArrayList()
                outSeries.forEach { chart.data.add(it) }
                chart.createSymbols = false

                with(chart.xAxis as TimeAxis) {
                    tickUnit = tickValue.toDouble()
                    lowerBound = range.startTime.toDouble()
                    upperBound = range.endTime.toDouble()
                }
            }

            previousVisibleRange = newVisibleRange
        }

    }

}
