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
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.xychart.control.XYChartControl
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.chart.XYChart
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineCap
import org.lttng.scope.common.jfx.TimeAxis
import org.lttng.scope.views.timeline.NavigationAreaWidget
import org.lttng.scope.views.timeline.TimelineWidget
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartDragHandlers
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartScrollHandlers
import org.lttng.scope.views.timeline.widgets.xychart.layer.XYChartSelectionLayer

/**
 * Widget for the timeline view showing data in a XY-Chart. The chart will
 * display data representing the whole trace, and will display highlighted
 * rectangles representing the current visible and selection time ranges.
 */
class XYChartFullRangeWidget(control: XYChartControl, override val weight: Int) : XYChartWidget(control), NavigationAreaWidget {

    companion object {
        /* Number of data poins we request to the backend */
        private const val NB_DATA_POINTS = 200
        private const val CHART_HEIGHT = 50.0

        private val VISIBLE_RANGE_STROKE_WIDTH = 4.0
        private val VISIBLE_RANGE_ARC = 5.0
        private val VISIBLE_RANGE_STROKE_COLOR = Color.GRAY
        private val VISIBLE_RANGE_FILL_COLOR = Color.LIGHTGRAY.deriveColor(0.0, 1.2, 1.0, 0.6)
    }

    override val rootNode = BorderPane()

    override val selectionLayer = XYChartSelectionLayer.build(this, -10.0)
    override val dragHandlers = XYChartDragHandlers(this)
    override val scrollHandlers = XYChartScrollHandlers(this)

    private val visibleRangeRect = object : Rectangle() {

        private val projectChangeListener = object : ViewGroupContext.ProjectChangeListener(this@XYChartFullRangeWidget) {
            override fun newProjectCb(newProject: TraceProject<*, *>?) {
                isVisible = (newProject != null)
            }
        }

        init {
            stroke = VISIBLE_RANGE_STROKE_COLOR
            strokeWidth = VISIBLE_RANGE_STROKE_WIDTH
            strokeLineCap = StrokeLineCap.ROUND
            fill = VISIBLE_RANGE_FILL_COLOR
            arcHeight = VISIBLE_RANGE_ARC
            arcWidth = VISIBLE_RANGE_ARC

            y = 0.0
            heightProperty().bind(Bindings.subtract(chart.heightProperty(), VISIBLE_RANGE_STROKE_WIDTH))

            val initialProject = viewContext.registerProjectChangeListener(projectChangeListener)
            isVisible = (initialProject != null)
        }

        @Suppress("ProtectedInFinal", "Unused")
        protected fun finalize() {
            viewContext.deregisterProjectChangeListener(projectChangeListener)
        }
    }

    init {
        /* Hide the axes in the full range chart */
        with(xAxis) {
            isTickLabelsVisible = false
            opacity = 0.0
        }

        with(yAxis) {
            isTickLabelsVisible = false
            opacity = 0.0
        }

        with(chart) {
            // setTitle(getName())
            padding = Insets(-10.0, -10.0, -10.0, -10.0)
            // setStyle("-fx-padding: 1px;")

            minHeight = CHART_HEIGHT
            prefHeight = CHART_HEIGHT
            maxHeight = CHART_HEIGHT
        }

        rootNode.center = StackPane(chart,
                Pane(visibleRangeRect).apply { isMouseTransparent = true },
                selectionLayer)

        rootNode.widthProperty().addListener { _, _, _ ->
            drawSelection(viewContext.selectionTimeRange)
            seekVisibleRange(viewContext.visibleTimeRange)
            timelineWidgetUpdateTask.run()
        }
    }

    override fun dispose() {
    }

    override fun getWidgetTimeRange() = viewContext.getCurrentProjectFullRange()

    override fun mapXPositionToTimestamp(x: Double): Long {
        val project = viewContext.traceProject ?: return 0L

        val viewWidth = chartPlotArea.width
        if (viewWidth < 1.0) return project.startTime

        val posRatio = x / viewWidth
        val ts = (project.startTime + posRatio * project.fullRange.duration).toLong()

        /* Clamp the result to the trace project's range. */
        return ts.coerceIn(project.fullRange.startTime, project.fullRange.endTime)
    }


    // ------------------------------------------------------------------------
    // TimelineWidget
    // ------------------------------------------------------------------------

    override val name = control.renderProvider.providerName

    override val timelineWidgetUpdateTask: TimelineWidget.TimelineWidgetUpdateTask = RedrawTask()

    /** Not applicable to this widget */
    override val splitPane = null

    /* Not applicable to this widget */
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
        /* Needs + 10 to be properly aligned, not sure why... */
        val xStart = xAxis.getDisplayPosition(newVisibleRange.startTime) + 10.0
        val xEnd = xAxis.getDisplayPosition(newVisibleRange.endTime) + 10.0
        if (xStart == Double.NaN || xEnd == Double.NaN) return

        with(visibleRangeRect) {
            x = xStart
            width = xEnd - xStart
        }
    }

    private inner class RedrawTask : TimelineWidget.TimelineWidgetUpdateTask {

        private var lastTraceProject: TraceProject<*, *>? = null

        override fun run() {
            /* Skip redraws if we are in a project-switching operation. */
            if (viewContext.listenerFreeze) return

            var newTraceProject = viewContext.traceProject
            if (newTraceProject == lastTraceProject) return

            if (newTraceProject == null) {
                /* Replace the list of series with an empty list */
                Platform.runLater { chart.data = FXCollections.observableArrayList() }
            } else {
                val painted = repaintChart(newTraceProject)
                if (!painted) {
                    newTraceProject = null
                }
            }
            lastTraceProject = newTraceProject
        }

        /**
         * Repaint the whole chart. Should only be necessary if the active trace
         * changes.
         *
         * @return If we have produced a "real" render or not. If not, it might be
         *         because the trace is still being initialized, so we should not
         *         consider it painted.
         */
        private fun repaintChart(traceProject: TraceProject<*, *>): Boolean {
            val traceFullRange = TimeRange.of(traceProject.startTime, traceProject.endTime)

            val renders = control.renderProvider.generateSeriesRenders(traceFullRange, NB_DATA_POINTS, null)
            if (renders.isEmpty()) return false

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

            xAxis.setTickStep(renders.first().resolutionX)

            Platform.runLater {
                chart.data = FXCollections.observableArrayList()
                outSeries.forEach { chart.data.add(it) }
                chart.createSymbols = false

                with(chart.xAxis as TimeAxis) {
                    tickUnit = range.duration.toDouble()
                    lowerBound = range.startTime.toDouble()
                    upperBound = range.endTime.toDouble()
                }
            }

            /*
             * On project-switching, the whole project time range should be the new visible range.
             *
             * Unfortunately, we cannot use the normal facilities here to redraw the visible range
             * rectangle because the data has not yet been rendered, so the axis does not yet
             * have its real width
             *
             * Instead use this ugly but working hack to artificially draw a rectangle that covers
             * the whole widget.
             */
            Platform.runLater {
                visibleRangeRect.apply {
                    isVisible = true
                    x = 10.0
                    width = chart.width - 10.0
                }
            }

            return true
        }

    }

}
