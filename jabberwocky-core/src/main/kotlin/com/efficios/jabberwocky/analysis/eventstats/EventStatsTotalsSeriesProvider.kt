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

        // TODO Support renders with less than 3 timestamps?
        if (timestamps.size < 3) throw IllegalArgumentException("XY Chart renders need a minimum of 3 datapoints, was $timestamps")

        val tsStateValues = ArrayList<Long>()

        for (ts in timestamps) {
            val queryTs = ts.coerceIn(ss.startTime, ss.currentEndTime)
            val sv = ss.querySingleState(queryTs, quark).stateValue
            tsStateValues.add(if (sv.isNull) 0L else (sv as IntegerStateValue).value.toLong())
        }

        val dataPoints = ArrayList<XYChartRender.DataPoint>()

        for (i in 0 until timestamps.size - 1) {
            val countDiffUntilNext = tsStateValues[i + 1] - tsStateValues[i]
            val ts = timestamps[i]

            dataPoints.add(XYChartRender.DataPoint(ts, countDiffUntilNext))
        }

        return dataPoints
    }

}
