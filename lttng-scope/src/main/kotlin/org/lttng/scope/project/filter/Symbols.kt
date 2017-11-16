/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.project.filter

import com.efficios.jabberwocky.views.common.EventSymbolStyle
import javafx.beans.property.ReadOnlyProperty
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.*

fun EventSymbolStyle.getGraphic(colorSource: ReadOnlyProperty<Color>): Node =
        getShape().apply { fillProperty().bind(colorSource) }


private fun EventSymbolStyle.getShape(): Shape {
    return when (this) {
        EventSymbolStyle.CIRCLE -> Circle(5.0)

        EventSymbolStyle.DIAMOND -> Polygon(5.0, 0.0,
                10.0, 5.0,
                5.0, 10.0,
                0.0, 5.0)
                .apply { relocate(-5.0, -5.0) }

        EventSymbolStyle.SQUARE -> Rectangle(-5.0, -5.0, 10.0, 10.0)

        // FIXME bigger?
        EventSymbolStyle.STAR -> Polygon(4.0, 0.0,
                5.0, 4.0,
                8.0, 4.0,
                6.0, 6.0,
                7.0, 9.0,
                4.0, 7.0,
                1.0, 9.0,
                2.0, 6.0,
                0.0, 4.0,
                3.0, 4.0)
                .apply { relocate(-4.0, -4.5) }

        EventSymbolStyle.TRIANGLE -> SVGPath().apply {
            content = "M5,0 L10,8 L0,8 Z"
            relocate(-5.0, -2.0)
        }

    // SymbolStyle.CROSS,
        else -> SVGPath().apply {
            content = "M2,0 L5,4 L8,0 L10,0 L10,2 L6,5 L10,8 L10,10 L8,10 L5,6 L2, 10 L0,10 L0,8 L4,5 L0,2 L0,0 Z"
            relocate(-5.0, -5.0)
        }

    }.apply {
        stroke = Color.BLACK
    }
}
