/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.model.provider

import com.efficios.jabberwocky.views.common.FlatUIColors
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender
import com.efficios.jabberwocky.views.xychart.model.render.XYChartSeries
import java.util.concurrent.FutureTask

class XYChartSeriesProviderStub : XYChartSeriesProvider(SERIES) {

    companion object {
        val SERIES = XYChartSeries("test-series", FlatUIColors.BLUE, XYChartSeries.LineStyle.FULL)
    }

    override fun fillSeriesRender(timestamps: List<Long>, task: FutureTask<*>?): List<XYChartRender.DataPoint>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
