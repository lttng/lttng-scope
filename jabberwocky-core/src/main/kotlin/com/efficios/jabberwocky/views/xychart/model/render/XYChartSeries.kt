/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.model.render

import com.efficios.jabberwocky.views.common.ColorDefinition

/**
 * Definition of a single series of an XY-Chart.
 */
data class XYChartSeries(val name: String,
                         val color: ColorDefinition,
                         val lineStyle: LineStyle,
                         val symbolStyle: SymbolStyle = XYChartSeries.SymbolStyle.NONE) {

    enum class LineStyle {
        /** Standard line */
        FULL,
        /** Dotted line */
        DOTTED,
        /** Color the line and the area under it */
        INTEGRAL
    }

    enum class SymbolStyle {
        // TODO NYI, we only use lines for now
        NONE
    }

}