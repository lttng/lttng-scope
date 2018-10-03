/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.model.render

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.views.common.ColorDefinition

/**
 * "Render" of a XY chart, containing all the relevant data points for a given
 * series, for a given time range.
 */
data class XYChartRender(val series: XYChartSeries,
                         val range: TimeRange,
                         val resolutionX: Long,
                         val data: List<DataPoint>) {

    companion object {
        val EMPTY_RENDER = XYChartRender(
                XYChartSeries("dummy", ColorDefinition(0, 0, 0), XYChartSeries.LineStyle.FULL),
                TimeRange.of(0, 0),
                0L,
                emptyList())
    }

    data class DataPoint(val x: Long, val y: Long)
}