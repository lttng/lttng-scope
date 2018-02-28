/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.project

import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.project.TraceProject
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import org.lttng.scope.project.filter.CreateEventFilterDialog
import org.lttng.scope.project.filter.EventFilterDefinition
import org.lttng.scope.project.filter.getGraphic
import org.lttng.scope.views.context.ViewGroupContextManager
import org.lttng.scope.common.jfx.JfxUtils

private const val NO_PROJECT = "(no project opened)"

private const val TRACES_NODE_NAME = "Traces"

private const val BOOKMARKS_NODE_NAME = "Bookmarks"

private const val FILTERS_NODE_NAME = "Filters"
private const val FILTERS_CREATE_FILTER = "Create Filter..."
private const val FILTERS_DELETE = "Delete Filter"


class ProjectArea : BorderPane() {

    private val projectTree = TreeView<String>()

    private val emptyProjectRootItem = TreeItem(NO_PROJECT)
    private val projectRootItem = TreeItem(NO_PROJECT).apply {
        isExpanded = true
    }

    init {
        /* Setup tree skeleton */
        val subItems = listOf(TracesTreeItem(),
                BookmarksTreeItem(),
                FiltersTreeItem(this))

        projectRootItem.children.addAll(subItems)
        projectTree.setCellFactory { ProjectTreeCell() }

        /* Setup listeners */
        ViewGroupContextManager.getCurrent().registerProjectChangeListener(object : ViewGroupContext.ProjectChangeListener {
            override fun newProjectCb(newProject: TraceProject<*, *>?) {
                JfxUtils.runLaterAndWait(Runnable {
                    if (newProject == null) {
                        projectTree.root = emptyProjectRootItem
                    } else {
                        projectRootItem.value = newProject.name
                        projectTree.root = projectRootItem
                    }
                })
            }
        })

        projectTree.root = emptyProjectRootItem
        center = projectTree
    }
}

private abstract class ProjectTreeItem(name: String) : TreeItem<String>(name) {

    private val projectChangeListener = object : ViewGroupContext.ProjectChangeListener {
        override fun newProjectCb(newProject: TraceProject<*, *>?) {
            if (newProject == null) {
                clear()
            } else {
                initForProject(newProject)
            }
        }
    }

    init {
        ViewGroupContextManager.getCurrent().registerProjectChangeListener(projectChangeListener)
        isExpanded = true
    }

    // Note this doesn't need 'override' in Kotlin, weird
    @Suppress("Unused")
    protected fun finalize() {
        // TODO Handle case where the Context we registered to might have changed
        ViewGroupContextManager.getCurrent().deregisterProjectChangeListener(projectChangeListener)
    }

    protected fun getCurrentProject() = ViewGroupContextManager.getCurrent().traceProject

    abstract fun initForProject(project: TraceProject<*, *>)

    open fun clear() {
        children.clear()
    }

    open val contextMenu: ContextMenu? = null
}

private class ProjectTreeCell : TreeCell<String>() {

    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty) {
            text = null
            graphic = null
        } else {
            text = getItem()?.toString() ?: ""
            graphic = treeItem.graphic
            if (treeItem is ProjectTreeItem) {
                contextMenu = (treeItem as ProjectTreeItem).contextMenu
            }
        }
    }
}

private class TracesTreeItem : ProjectTreeItem(TRACES_NODE_NAME) {

    override fun initForProject(project: TraceProject<*, *>) {
        val traces = project.traceCollections.flatMap { it.traces }
        val newChildren = traces.map { TreeItem(it.name) }

        children.clear()
        children.addAll(newChildren)
    }
}

private class BookmarksTreeItem : ProjectTreeItem(BOOKMARKS_NODE_NAME) {
    override fun initForProject(project: TraceProject<*, *>) {
        // TODO
    }
}

private class FiltersTreeItem(refNode: Node) : ProjectTreeItem(FILTERS_NODE_NAME), ProjectFilters.FilterListener {

    private inner class FilterTreeItem(val filterDef: EventFilterDefinition) : ProjectTreeItem(filterDef.name) {

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

    override val contextMenu: ContextMenu

    init {
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
