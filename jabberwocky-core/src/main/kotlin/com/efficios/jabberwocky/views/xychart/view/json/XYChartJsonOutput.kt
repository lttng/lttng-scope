/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.view.json

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.views.xychart.control.XYChartControl
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender
import com.efficios.jabberwocky.views.xychart.view.XYChartView
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream

class XYChartJsonOutput(override val control: XYChartControl) : XYChartView {

    companion object {
        private const val VERSION = 1
        private const val PRETTY_PRINT = true
        private const val VERSION_KEY = "version"

        private const val SERIES_KEY = "series"
        private const val SERIES_NAME_KEY = "series-name"
        private const val SERIES_COLOR_KEY = "series-color"
        private const val SERIES_LINESTYLE_KEY = "series-linestyle"
        private const val SERIES_SYMBOLSTYLE_KEY = "series-symbolstyle"

        private const val DATAPOINTS_KEY = "datapoints"
        private const val DATAPOINT_X_KEY = "x"
        private const val DATAPOINT_Y_KEY = "y"

        fun printRenderTo(os: OutputStream, renders: List<XYChartRender>) {
            os.bufferedWriter().use { bw ->
                val root = JSONObject()
                root.put(VERSION_KEY, VERSION)

                val seriesRoot = JSONArray()
                for (render in renders) {
                    val seriesObject = JSONObject()
                    seriesObject.put(SERIES_NAME_KEY, render.series.name)
                    seriesObject.put(SERIES_COLOR_KEY, render.series.color)
                    seriesObject.put(SERIES_LINESTYLE_KEY, render.series.lineStyle)
                    seriesObject.put(SERIES_SYMBOLSTYLE_KEY, render.series.symbolStyle)

                    val datapointsRoot = JSONArray()
                    for ((x, y) in render.data) {
                        val datapointObject = JSONObject()
                        datapointObject.put(DATAPOINT_X_KEY, x)
                        datapointObject.put(DATAPOINT_Y_KEY, y)
                        datapointsRoot.put(datapointObject)
                    }
                    seriesObject.put(DATAPOINTS_KEY, datapointsRoot)

                    seriesRoot.put(seriesObject)
                }
                root.put(SERIES_KEY, seriesRoot)

                val json = if (PRETTY_PRINT) root.toString(1) else root.toString()
                bw.write(json)
                bw.flush()

            }
        }
    }

    override fun dispose() {
        /* Nothing to dispose */
    }

    override fun clear() {
        TODO("not implemented")
    }

    override fun seekVisibleRange(newVisibleRange: TimeRange) {
        /* Generate JSON for the visible area */
        val renders = control.renderProvider.generateSeriesRenders(newVisibleRange, 10, null)
        printRenderTo(System.out, renders)
    }

    override fun drawSelection(selectionRange: TimeRange) {
        TODO("not implemented")
    }

}

