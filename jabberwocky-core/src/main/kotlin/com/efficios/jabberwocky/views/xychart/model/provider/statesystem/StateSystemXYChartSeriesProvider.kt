/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.model.provider.statesystem

import com.efficios.jabberwocky.views.xychart.model.provider.XYChartSeriesProvider
import com.efficios.jabberwocky.views.xychart.model.render.XYChartSeries

abstract class StateSystemXYChartSeriesProvider(series: XYChartSeries,
                                                protected val modelProvider: StateSystemXYChartProvider) : XYChartSeriesProvider(series)
