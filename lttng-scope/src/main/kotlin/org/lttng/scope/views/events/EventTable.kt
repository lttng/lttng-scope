/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.events

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.trace.event.TraceEvent
import com.sun.javafx.scene.control.skin.TableViewSkin
import com.sun.javafx.scene.control.skin.VirtualFlow
import javafx.application.Platform
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.scene.CacheHint
import javafx.scene.Node
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import org.lttng.scope.application.ScopeOptions
import org.lttng.scope.project.ProjectFilters
import org.lttng.scope.project.ProjectManager
import org.lttng.scope.project.filter.EventFilterDefinition
import org.lttng.scope.project.filter.getGraphic
import org.lttng.scope.views.context.ViewGroupContextManager

/**
 * Table displaying the trace project's trace events.
 */
class EventTable(private val tableControl: EventTableControl) : BorderPane() {

    internal val tableView: TableView<TraceEvent>

    init {
        /* Setup the table */
        val filterIconsCol = FilterColumn()
        val timestampCol = createTextColumn("Timestamp", 200.0, { ScopeOptions.timestampFormat.tsToString(it.timestamp) })
        val traceCol = createTextColumn("Trace", 100.0, { it.trace.name })
        val cpuCol = createTextColumn("CPU", 50.0, { it.cpu.toString() })
        val typeCol = createTextColumn("Event Type", 200.0, { it.eventName })
        val fieldsCol = createTextColumn("Event Fields", 800.0, { event ->
            event.fields
                    .map { "${it.key}=${it.value}" }
                    .toString()
        })

        tableView = TableView<TraceEvent>().apply {
            fixedCellSize = 24.0
            isCache = true
            cacheHint = CacheHint.SPEED
            columns.addAll(filterIconsCol, timestampCol, traceCol, cpuCol, typeCol, fieldsCol)

            /* Row factory to add the click listener on each row */
            setRowFactory {
                TableRow<TraceEvent>().apply {
                    setOnMouseClicked { this.item?.let { updateSelection(it.timestamp) } }
                }
            }
        }

        center = tableView
        right = EventTableScrollToolBar(tableControl)
    }

    private fun createTextColumn(headerText: String, initialWidth: Double, provideText: (TraceEvent) -> String): TableColumn<TraceEvent, String> {
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

                /*
                 * Scroll to the target index, but only if it is out of the current
                 * range shown by the table.
                 */
                val visibleRows = getVisibleRowIndices() ?: return@runLater
                if (index !in visibleRows) {
                    /* Place the target row in the middle of the view. */
                    scrollTo(maxOf(0, index - visibleRows.count() / 2))
                }
            }
        }
    }

    /**
     * Refresh the view, as in, keep the same displayed data and selection,
     * but ask to regenerate the cell contents.
     */
    fun refresh() {
        tableView.refresh()
    }

    /**
     * Whenever a table row is clicked, the selection in the rest
     * of the framework should be updated.
     *
     * If we end up pointing to a timestamp outside of the current
     * visible range, also recenter the visible range on that timestamp
     * (but *only* if it's outside, else don't move the visible range).
     */
    private fun updateSelection(timestamp: Long) {
        val viewCtx = tableControl.viewContext
        viewCtx.selectionTimeRange = TimeRange.of(timestamp, timestamp)

        if (timestamp !in viewCtx.visibleTimeRange) {
            viewCtx.centerVisibleRangeOn(timestamp)
        }
    }

    /**
     * Get the start/end range currently shown by the table, in terms of row index.
     *
     * If the table is empty then null is returned.
     */
    private fun getVisibleRowIndices(): IntRange? {
        val flow = (tableView.skin as TableViewSkin<*>).children[1] as VirtualFlow<*>
        val firstCell = flow.firstVisibleCell ?: return null
        val lastCell = flow.lastVisibleCell ?: return null
        return firstCell.index..lastCell.index
    }
}

private class FilterColumn : TableColumn<TraceEvent, TraceEvent>(""), ProjectFilters.FilterListener {

    private inner class FilterCell : TableCell<TraceEvent, TraceEvent>() {

        private val filterSymbols = mutableMapOf<EventFilterDefinition, Node>()
        private val node = HBox()

        init {
            enabledFilters.addListener(ListChangeListener<EventFilterDefinition> { c ->
                if (c == null) return@ListChangeListener
                while (c.next()) {
                    for (removedFilter in c.removed) {
                        filterSymbols.remove(removedFilter)?.let { node.children.remove(it) }
                    }
                    for (addedFilter in c.addedSubList) {
                        val graphic = addedFilter.getGraphic()
                        /*
                         * Whenever a symbol is hidden, we don't want it to count in its
                         * parent's layout calculations (so the HBox "collapses").
                         */
                        graphic.managedProperty().bind(graphic.visibleProperty())
                        filterSymbols.put(addedFilter, graphic)
                        graphic.isVisible = (item != null && addedFilter.predicate.invoke(item))
                        node.children.add(graphic)
                    }
                }
            })
        }

        override fun updateItem(item: TraceEvent?, empty: Boolean) {
            super.updateItem(item, empty)
            filterSymbols.forEach { filter, node ->
                node.isVisible = (item != null && filter.predicate.invoke(item))
            }
            graphic = node
        }
    }

    private val createdFilters = mutableListOf<EventFilterDefinition>()
    private val enabledFilters = FXCollections.observableArrayList<EventFilterDefinition>()

    init {
        ViewGroupContextManager.getCurrent().registerProjectChangeListener(object : ViewGroupContext.ProjectChangeListener {
            override fun newProjectCb(newProject: TraceProject<*, *>?) {
                if (newProject != null) {
                    ProjectManager.getProjectState(newProject).filters.registerFilterListener(this@FilterColumn)
                } else {
                    createdFilters.clear()
                    enabledFilters.clear()
                }
            }
        })
                /* The 'register' call returns the current project */
                ?.let {
                    ProjectManager.getProjectState(it).filters.registerFilterListener(this)
                }

        setCellValueFactory { ReadOnlyObjectWrapper(it.value) }
        setCellFactory { FilterCell() }
        isSortable = false
        prefWidth = 20.0
    }

    override fun filterCreated(filter: EventFilterDefinition) {
        createdFilters.add(filter)
        if (filter.isEnabled) {
            enabledFilters.add(filter)
        }
        filter.enabledProperty().addListener { _, _, nowEnabled ->
            if (nowEnabled) {
                enabledFilters.add(filter)
            } else {
                enabledFilters.remove(filter)
            }
        }
    }

    override fun filterRemoved(filter: EventFilterDefinition) {
        createdFilters.remove(filter)
        enabledFilters.remove(filter)
    }
}
