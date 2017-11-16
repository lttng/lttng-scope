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
import javafx.beans.property.SimpleBooleanProperty

data class EventFilterDefinition(val name: String,
                                 val color: ColorDefinition,
                                 val symbol: EventSymbolStyle,
                                 val predicate: (TraceEvent) -> Boolean) {

    private val enabledProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun enabledProperty() = enabledProperty
    var isEnabled
        get() = enabledProperty.get()
        set(value) = enabledProperty.set(value)

}
