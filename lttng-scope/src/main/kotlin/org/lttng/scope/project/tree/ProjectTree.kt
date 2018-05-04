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
import javafx.scene.control.ContextMenu
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import org.lttng.scope.application.actions.editProject
import org.lttng.scope.common.jfx.JfxUtils
import org.lttng.scope.common.jfx.ScopeMenuItem
import org.lttng.scope.views.context.ViewGroupContextManager


class ProjectTree : TreeView<String>() {

    companion object {
        private const val NO_PROJECT = "(no project opened)"
        private const val PROJECT_SETUP_ACTION = "Project Setup"
    }

    private val emptyProjectRootItem = TreeItem(NO_PROJECT)
    private val projectRootItem = RootTreeItem()

    init {
        setCellFactory { ProjectTreeCell() }

        /* Setup listeners */
        ViewGroupContextManager.getCurrent().registerProjectChangeListener(object : ViewGroupContext.ProjectChangeListener(this) {
            override fun newProjectCb(newProject: TraceProject<*, *>?) {
                JfxUtils.runLaterAndWait(Runnable {
                    root = if (newProject == null) {
                        emptyProjectRootItem
                    } else {
                        projectRootItem
                    }
                })
            }
        })

        root = emptyProjectRootItem
    }

    private inner class RootTreeItem : ProjectTreeItem(NO_PROJECT) {

        override val contextMenu: ContextMenu

        override fun initForProject(project: TraceProject<*, *>) {
            value = project.name
        }

        override fun clear() {
            /* Do not call super.clear() (which clears the children, which we don't want here!) */
        }

        init {
            val projectSetupMenuItem = ScopeMenuItem(PROJECT_SETUP_ACTION) {
                getCurrentProject()?.let { editProject(this@ProjectTree, it) }
            }

            contextMenu = ContextMenu(projectSetupMenuItem)

            /* Setup tree skeleton */
            children.addAll(TracesTreeItem(),
//                BookmarksTreeItem(),
                    FiltersTreeItem(this@ProjectTree))
        }

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
