/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.project.tree

import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.project.TraceProject
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import org.lttng.scope.common.jfx.JfxUtils
import org.lttng.scope.views.context.ViewGroupContextManager

class ProjectTree : TreeView<String>() {

    companion object {
        private const val NO_PROJECT = "(no project opened)"
    }

    private val emptyProjectRootItem = TreeItem(NO_PROJECT)
    private val projectRootItem = TreeItem(NO_PROJECT).apply {
        isExpanded = true
    }

    init {
        /* Setup tree skeleton */
        projectRootItem.children.addAll(TracesTreeItem(),
//                BookmarksTreeItem(),
                FiltersTreeItem(this))

        setCellFactory { ProjectTreeCell() }

        /* Setup listeners */
        ViewGroupContextManager.getCurrent().registerProjectChangeListener(object : ViewGroupContext.ProjectChangeListener(this) {
            override fun newProjectCb(newProject: TraceProject<*, *>?) {
                JfxUtils.runLaterAndWait(Runnable {
                    if (newProject == null) {
                        root = emptyProjectRootItem
                    } else {
                        projectRootItem.value = newProject.name
                        root = projectRootItem
                    }
                })
            }
        })

        root = emptyProjectRootItem
    }
}

private class ProjectTreeCell : TreeCell<String>() {

    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)

        val treeItem = treeItem

        if (empty) {
            text = null
            graphic = null
            contextMenu = null
            tooltip = null
        } else {
            text = getItem()?.toString() ?: ""
            graphic = treeItem.graphic

            if (treeItem is ProjectTreeItem) {
                contextMenu = treeItem.contextMenu
                tooltip = treeItem.tooltip
            }
        }
    }
}
