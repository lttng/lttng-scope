/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.analysis.eventstats

import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException
import ca.polymtl.dorsal.libdelorean.statevalue.IntegerStateValue
import com.efficios.jabberwocky.views.common.FlatUIColors
import com.efficios.jabberwocky.views.xychart.model.provider.statesystem.StateSystemXYChartSeriesProvider
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender
import com.efficios.jabberwocky.views.xychart.model.render.XYChartSeries
import java.util.concurrent.FutureTask

class EventStatsTotalsSeriesProvider(modelProvider: EventStatsXYChartProvider) : StateSystemXYChartSeriesProvider(EVENTS_TOTAL_SERIES, modelProvider) {


    companion object {
        private val EVENTS_TOTAL_SERIES = XYChartSeries("Events total", FlatUIColors.DARK_BLUE, XYChartSeries.LineStyle.INTEGRAL)
    }

    override fun fillSeriesRender(timestamps: List<Long>, task: FutureTask<*>?): List<XYChartRender.DataPoint>? {
        val ss = modelProvider.stateSystem
        if (ss == null || (task?.isCancelled == true)) {
            return null
        }

        val quark: Int = try {
            ss.getQuarkAbsolute(EventStatsAnalysis.TOTAL_ATTRIBUTE)
        } catch (e: AttributeNotFoundException) {
            return null
        }

        /*
         * For each requested timestamp, we will give a number of events that are present inside a
         * "bucket" centered on the timestamp. Next we need to determine the borders of those buckets.
         *
         * For example, if the requested timestamps are [4, 6, 8, 10], then we will define buckets with
         * borders: [3, 5, 7, 9, 11].
         */
        // TODO Support renders with less than 3 timestamps?
        if (timestamps.size < 3) throw IllegalArgumentException("XY Chart renders need a minimum of 3 datapoints, was $timestamps")

        val bucketBorders = mutableListOf<Long>()
        /* First bucket's start depends on the first 2 timestamps */
        (timestamps[1] - timestamps[0])
                .let { timestamps[0] - it / 2 }
                .let { bucketBorders.add(it) }

        /* Middle buckets are cut halfway of the distance between each timestamp. Note we start at 1 not 0. */
        for (i in 1 until timestamps.size) {
            (timestamps[i] - timestamps[i - 1])
                    .let { timestamps[i] - it / 2 }
                    .let { bucketBorders.add(it) }
        }

        /* Last bucket's end depends on the last 2 timestamps */
        (timestamps.last() - timestamps[timestamps.size - 1])
                .let { timestamps.last() + it / 2 }
                .let { bucketBorders.add(it) }

        /* Map <timestamp, count> each bucket border to the amount of events seen so far. */
        val eventCounts = bucketBorders
                .map { it.coerceIn(ss.startTime, ss.currentEndTime) }
                .map { ts ->
                    val sv = ss.querySingleState(ts, quark).stateValue
                    val count = if (sv.isNull) 0L else (sv as IntegerStateValue).value.toLong()
                    ts to count
                }

        /* Assert everything is as expected */
        if (eventCounts.size != timestamps.size + 1) throw IllegalStateException()

        /* Compute the number of events in each "bucket" */
        val bucketCounts = mutableListOf<Pair<Long, Long>>() // Pair<ts, count>
        for (i in 1 until eventCounts.size) {
            val count = eventCounts[i].second - eventCounts[i - 1].second
            bucketCounts.add(timestamps[i - 1] to count)
        }

        return bucketCounts.map { XYChartRender.DataPoint(it.first, it.second) }
    }

}
