/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.project

import com.efficios.jabberwocky.project.TraceProject
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.BorderPane
import org.lttng.scope.views.context.ViewGroupContextManager

private const val TRACES_NODE_NAME = "Traces"
private const val BOOKMARKS_NODE_NAME = "Bookmarks"
private const val FILTERS_NODE_NAME = "Filters"
private const val SEARCHES_NODE_NAME = "Recent Searches"

private const val NO_PROJECT = "(no project opened)"

class ProjectArea : BorderPane() {

    private val projectTree = TreeView<String>()

    private val emptyProjectItem = TreeItem(NO_PROJECT)
    private val projectRootItem = TreeItem(NO_PROJECT).apply {
        isExpanded = true
    }

    init {
        /* Setup tree skeleton */
        val tracesTreeItem = TracesTreeItem()
        val bookmarksTreeItem = TreeItem(BOOKMARKS_NODE_NAME)
        val filtersTreeItem = TreeItem(FILTERS_NODE_NAME)
        val searchesTreeItem = TreeItem(SEARCHES_NODE_NAME)

        projectRootItem.children.addAll(tracesTreeItem,
                bookmarksTreeItem,
                filtersTreeItem,
                searchesTreeItem)

        /* Setup listeners */
        val viewCtx = ViewGroupContextManager.getCurrent()
        viewCtx.currentTraceProjectProperty().addListener { _, _, newProject ->
            if (newProject == null) {
                projectTree.root = emptyProjectItem
                tracesTreeItem.children.clear()
                // TODO Clear other sub-trees
            } else {
                projectRootItem.value = newProject.name
                projectTree.root = projectRootItem
                tracesTreeItem.updateTraces(newProject)
            }
        }

        projectTree.root = emptyProjectItem
        center = projectTree
    }
}

private class TracesTreeItem : TreeItem<String>(TRACES_NODE_NAME) {
    fun updateTraces(project: TraceProject<*, *>) {
        val traces = project.traceCollections.flatMap { it.traces }
        val newChildren = traces.map { TreeItem(it.name) }

        children.clear()
        children.addAll(newChildren)
        isExpanded = true
    }
}