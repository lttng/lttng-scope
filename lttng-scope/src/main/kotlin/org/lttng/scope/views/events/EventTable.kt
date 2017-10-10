/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.events

import com.efficios.jabberwocky.trace.event.TraceEvent
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane

/**
 * Table displaying the trace project's trace events.
 *
 * TODO NYI
 */
class EventTable : BorderPane() {

    init {
        val col1 = TableColumn<TraceEvent, String>("Event table will go here!")
        col1.prefWidth = 200.0

        val table = TableView<TraceEvent>()
        table.columns.add(col1)

        center = table
    }
}