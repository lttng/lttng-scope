/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application.actions

import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.trace.Trace
import javafx.beans.binding.Bindings
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.util.Callback
import org.lttng.scope.ScopePaths
import org.lttng.scope.common.jfx.ActionButton
import java.nio.file.Files
import java.util.*
import kotlin.math.absoluteValue


class ProjectSetupDialog(private val refNode: Node, previousProject: TraceProject<*, *>?) : Dialog<TraceProject<*, *>?>() {

    companion object {
        private const val DIALOG_TITLE = "Trace Project Setup"
        private const val ADD_TRACE_BUTTON_TEXT = "Add Trace..."
        private const val REMOVE_TRACE_BUTTON_TEXT = "Remove Trace"
        private const val PROJECT_NAME_FIELD = "Project Name (optional)"

        private const val DIALOG_INITIAL_WIDTH = 800.0
        private const val SPACING = 10.0
    }

    private val trackedTraces: ObservableList<Trace<*>> = FXCollections.observableArrayList()

    private val table = TracesTableView(trackedTraces)

    private val addTraceButton = ActionButton(ADD_TRACE_BUTTON_TEXT) { addTraceAction() }
    private val removeTraceButton = ActionButton(REMOVE_TRACE_BUTTON_TEXT) { removeTraceAction() }

    private val buttonsColumn = VBox(addTraceButton, removeTraceButton).apply {
        children.forEach {
            it as Button
            it.minWidthProperty().bind(this.widthProperty())
        }
    }

    private val projectNameField = TextField().apply {
        promptText = PROJECT_NAME_FIELD
    }

    private val content = BorderPane().apply {
        center = table
        right = buttonsColumn
        bottom = projectNameField

        /* Add some padding between the table and the other nodes (but not with the window borders). */
        BorderPane.setMargin(table, Insets(0.0, SPACING, SPACING, 0.0))
    }


    init {
        title = DIALOG_TITLE
        isResizable = false
        dialogPane.content = content
        dialogPane.buttonTypes.addAll(ButtonType.CANCEL, ButtonType.OK)
        dialogPane.minWidth = DIALOG_INITIAL_WIDTH

        /* Activate the "Remove Trace" button only when there is a selection. */
        removeTraceButton.disableProperty().bind(Bindings.isEmpty(table.selectionModel.selectedItems))

        setResultConverter { buttonType ->
            when (buttonType) {
                ButtonType.OK -> {
                    val traces = trackedTraces.toList()
                    /* Do not create a project if there are no traces, just cancel. */
                    if (traces.isEmpty()) return@setResultConverter null

                    /* Fetch the name from the text field, if there is none we will create one. */
                    val requestedName = projectNameField.text
                    val projectName = if (requestedName.isNullOrBlank()) {
                        "project-${traces.traceCollectionHash()}"
                    } else {
                        requestedName
                    }
                    createProject(projectName, traces)
                }
                else -> null
            }
        }
    }

    fun addTraceAction() {
        val trace = askUserForTrace(refNode) ?: return
        /*
         * Avoid duplicate traces in the same project. We could have used a Set, but the
         * TableView's model works with a List...
         */
        if (trackedTraces.any { (it as? CtfTrace)?.tracePath == trace.tracePath }) return

        trackedTraces.add(trace)
    }

    private fun removeTraceAction() {
        val tracesToRemove = table.selectionModel.selectedItems.toList()
        trackedTraces.removeAll(tracesToRemove)
    }

}

private fun createProject(projectName: String, traces: Collection<Trace<*>>): TraceProject<*, *> {
    val projectPath = ScopePaths.projectsDir.resolve(projectName)
    if (!Files.exists(projectPath)) Files.createDirectories(projectPath)

    // For now, we only use a single trace collection for all traces in a project
    val traceCollection = TraceCollection(traces)
    return TraceProject(projectName, projectPath, listOf(traceCollection))
}


/** Build a 'hashCode' only from the traces contained in a project. */
private fun List<Trace<*>>.traceCollectionHash(): Int {
    val hashes = this.map { it.hash(it.name) }
    return Arrays.hashCode(hashes.toIntArray()).absoluteValue
}

/*
 * Get a unique project name (string) for a given trace.
 *
 * TODO Ideally we should add the number of events into this hash.
 */
private fun Trace<*>.hash(traceName: String): Int = Objects.hash(traceName, startTime, endTime)

private class TracesTableView(dataset: ObservableList<Trace<*>>) : TableView<Trace<*>>() {

    companion object {
        private const val TRACE_NAME_COL = "Trace Name"
        private const val TRACE_PATH_COL = "Path"
        private const val UNKNOWN = "???"
    }

    init {
        selectionModel.selectionMode = SelectionMode.MULTIPLE

        /* Create and setup the columns */
        val traceNameCol = TableColumn<Trace<*>, String>(TRACE_NAME_COL).apply {
            cellValueFactory = Callback { ReadOnlyStringWrapper(it.value.name) }
            prefWidthProperty().bind(this@TracesTableView.widthProperty().multiply(0.2))
        }

        val tracePathCol = TableColumn<Trace<*>, String>(TRACE_PATH_COL).apply {
            cellValueFactory = Callback {
                val trace = it.value
                val pathToDisplay = (trace as? CtfTrace)?.tracePath?.toString() ?: UNKNOWN
                ReadOnlyStringWrapper(pathToDisplay)
            }
            prefWidthProperty().bind(this@TracesTableView.widthProperty().multiply(0.75))
        }
        columns.addAll(traceNameCol, tracePathCol)
        itemsProperty().bind(SimpleListProperty(dataset))
    }

}
