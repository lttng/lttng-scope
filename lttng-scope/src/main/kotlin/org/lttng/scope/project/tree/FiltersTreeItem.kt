/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.project.tree

import com.efficios.jabberwocky.project.TraceProject
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import org.lttng.scope.common.jfx.JfxUtils
import org.lttng.scope.project.ProjectFilters
import org.lttng.scope.project.ProjectManager
import org.lttng.scope.project.filter.CreateEventFilterDialog
import org.lttng.scope.project.filter.EventFilterDefinition
import org.lttng.scope.project.filter.getGraphic

internal class FiltersTreeItem(refNode: Node) : ProjectTreeItem(FILTERS_NODE_NAME), ProjectFilters.FilterListener {

    companion object {
        private const val FILTERS_NODE_NAME = "Filters"
        private const val FILTERS_NODE_TOOLTIP = "Manage project-wide filters"

        private const val FILTERS_CREATE_FILTER = "Create Filter..."
    }

    override val contextMenu: ContextMenu
    override val tooltip: Tooltip

    init {
        tooltip = Tooltip(FILTERS_NODE_TOOLTIP)

        val createNewFilterMenuItem = MenuItem(FILTERS_CREATE_FILTER).apply {
            setOnAction {
                val project = getCurrentProject() ?: return@setOnAction
                val optFilter = with(CreateEventFilterDialog()) {
                    setOnShowing { Platform.runLater { JfxUtils.centerDialogOnScreen(this, refNode) } }
                    showAndWait()
                }
                if (optFilter.isPresent) {
                    ProjectManager.getProjectState(project).filters.createFilter(optFilter.get())
                }
            }
        }

        contextMenu = ContextMenu(createNewFilterMenuItem)
    }

    override fun initForProject(project: TraceProject<*, *>) {
        children.clear()
        ProjectManager.getProjectState(project).filters.registerFilterListener(this)
    }

    override fun filterCreated(filter: EventFilterDefinition) {
        children.add(FilterTreeItem(filter))
    }

    override fun filterRemoved(filter: EventFilterDefinition) {
        val childItem = children.find { (it as FilterTreeItem).filterDef == filter } ?: return
        children.remove(childItem)
    }

}

private class FilterTreeItem(val filterDef: EventFilterDefinition) : ProjectTreeItem(filterDef.name) {

    companion object {
        private const val FILTERS_DELETE = "Delete Filter"
    }

    override fun initForProject(project: TraceProject<*, *>) {
        /* Nothing to do, parent will delete us anyway. */
    }

    override val contextMenu: ContextMenu

    init {
        /*
         * We will use a HBox with the checkbox and the filter's symbol.
         * CheckBoxTreeCell is not very appropriate here, it's more for when you want to
         * select sub-trees when clicking a parent.
         */
        val checkbox = CheckBox().apply { selectedProperty().bindBidirectional(filterDef.enabledProperty()) }
        /* For now, the graphic won't change for a given filter */
        val symbol = filterDef.getGraphic()
        graphic = HBox(checkbox, symbol).apply { alignment = Pos.CENTER }

        /* Setup the context menu */
        val removeFilterMenuItem = MenuItem(FILTERS_DELETE).apply {
            setOnAction {
                val project = getCurrentProject() ?: return@setOnAction
                ProjectManager.getProjectState(project).filters.removeFilter(filterDef)
            }
        }

        contextMenu = ContextMenu(removeFilterMenuItem)
    }
}
