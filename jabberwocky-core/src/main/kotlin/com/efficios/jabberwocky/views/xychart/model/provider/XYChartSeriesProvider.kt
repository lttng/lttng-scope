/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.model.provider

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.views.xychart.model.XYChartResolutionUtils
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender
import com.efficios.jabberwocky.views.xychart.model.render.XYChartSeries
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import java.util.concurrent.FutureTask
import kotlin.math.max

abstract class XYChartSeriesProvider(val series: XYChartSeries) {

    /** Indicate if this series is enabled or not. */
    private val enabledProperty: BooleanProperty = SimpleBooleanProperty(true)

    fun enabledProperty() = enabledProperty
    var enabled
        get() = enabledProperty.get()
        set(value) = enabledProperty.set(value)

    fun generateSeriesRender(range: TimeRange, numberOfDataPoints: Int, task: FutureTask<*>? = null): XYChartRender {
        if (numberOfDataPoints < 3) throw IllegalArgumentException("XYChart render needs at least 3 data points.")

        /**
         * We want similar queries to "stay aligned" on a sort of grid, so that slight deviations do
         * not cause aliasing. First compute the step of the grid we will use, then the start time
         * to use (which will be earlier or equal to the query's time range). Then compute the actual
         * timestamps.
         */

        /* Determine the step */
        val roughResolution = max(1.0, range.duration / (numberOfDataPoints - 1.0))
        val step = XYChartResolutionUtils.GRID_RESOLUTIONS.floor(roughResolution.toLong())
                ?: XYChartResolutionUtils.GRID_RESOLUTIONS.first()!!

        /* Determine the start time of the target range. Simply "star_time - (start_time % step). */
        val renderStart = range.startTime - (range.startTime % step)

        val timestamps = (renderStart..range.endTime step step).toList()
        if (timestamps.size <= 1) {
            /* We don't even have a range, so there should be no events returned. */
            return XYChartRender.EMPTY_RENDER
        }

        val datapoints = fillSeriesRender(timestamps, task) ?: return XYChartRender.EMPTY_RENDER
        return XYChartRender(series, range, step, datapoints)
    }

    protected abstract fun fillSeriesRender(timestamps: List<Long>, task: FutureTask<*>? = null): List<XYChartRender.DataPoint>?
}
