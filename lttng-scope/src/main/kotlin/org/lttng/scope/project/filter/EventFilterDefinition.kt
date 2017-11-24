/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.project.filter

import com.efficios.jabberwocky.trace.event.TraceEvent
import com.efficios.jabberwocky.views.common.ColorDefinition
import com.efficios.jabberwocky.views.common.EventSymbolStyle
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.paint.Color
import org.lttng.scope.views.jfx.JfxColorFactory

data class EventFilterDefinition(val name: String,
                            private val initialColor: ColorDefinition,
                            val symbol: EventSymbolStyle,
                            val predicate: (TraceEvent) -> Boolean) {

    private val colorProperty: ObjectProperty<Color> = SimpleObjectProperty(JfxColorFactory.getColorFromDef(initialColor))
    fun colorProperty() = colorProperty
    var color: Color
        get() = colorProperty.get()
        set(value) = colorProperty.set(value)

    private val enabledProperty: BooleanProperty = SimpleBooleanProperty(true)
    fun enabledProperty() = enabledProperty
    var isEnabled
        get() = enabledProperty.get()
        set(value) = enabledProperty.set(value)

}

fun EventFilterDefinition.getGraphic(): Node = this.symbol.getGraphic(this.colorProperty())