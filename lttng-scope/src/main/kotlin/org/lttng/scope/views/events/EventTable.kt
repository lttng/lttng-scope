/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.events

import com.efficios.jabberwocky.trace.event.FieldValue
import com.efficios.jabberwocky.trace.event.TraceEvent
import javafx.application.Platform
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.scene.CacheHint
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import org.lttng.scope.views.timecontrol.TimestampConversion

/**
 * Table displaying the trace project's trace events.
 */
class EventTable(tableControl: EventTableControl) : BorderPane() {

    private val tableView: TableView<TraceEvent>

    init {
        /* Setup the table */
        val timestampCol = createColumn("Timestamp", 200.0, { TimestampConversion.tsToString(it.timestamp) })
        val traceCol = createColumn("Trace", 100.0, { it.trace.name })
        val cpuCol = createColumn("CPU", 50.0, { it.cpu.toString() })
        val typeCol = createColumn("Event Type", 200.0, { it.eventName })
        val fieldsCol = createColumn("Event Fields", 800.0, { event ->
            event.fieldNames
                    .map { fieldName -> "$fieldName=${event.getField(fieldName, FieldValue::class.java)}" }
                    .toString()
        })

        tableView = TableView<TraceEvent>().apply {
            fixedCellSize = 24.0
            isCache = true
            cacheHint = CacheHint.SPEED
            columns.addAll(timestampCol, traceCol, cpuCol, typeCol, fieldsCol)
        }

        center = tableView

        right = EventTableScrollToolBar(tableControl)
    }

    private fun createColumn(headerText: String, initialWidth: Double, provideText: (TraceEvent) -> String): TableColumn<TraceEvent, String> {
        return TableColumn<TraceEvent, String>(headerText).apply {
            setCellValueFactory { ReadOnlyObjectWrapper(provideText(it.value)) }
            isSortable = false
            prefWidth = initialWidth
        }
    }

    fun clearTable() {
        tableView.items = FXCollections.emptyObservableList()
    }

    fun displayEvents(events: List<TraceEvent>) {
        tableView.items = FXCollections.observableList(events)
    }

    /**
     * Scroll to the top *of the current event list*.
     * This method will not load any new events from the trace.
     */
    fun scrollToTop() {
        Platform.runLater { tableView.scrollTo(0) }
    }

    /**
     * Scroll to the end *of the current event list*.
     * This method will not load any new events from the trace.
     */
    fun scrollToBottom() {
        val nbItems = tableView.items.size
        Platform.runLater { tableView.scrollTo(nbItems - 1) }
    }

    fun selectIndex(index: Int) {
        Platform.runLater {
            with(tableView) {
                requestFocus()
                selectionModel.clearAndSelect(index)
                focusModel.focus(index)
                scrollTo(maxOf(0, index - 3))
            }
        }
    }
}
