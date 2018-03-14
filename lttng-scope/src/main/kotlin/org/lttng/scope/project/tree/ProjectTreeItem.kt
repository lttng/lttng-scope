/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
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
import javafx.scene.control.TreeItem
import org.lttng.scope.views.context.ViewGroupContextManager

internal abstract class ProjectTreeItem(name: String) : TreeItem<String>(name) {

    private val projectChangeListener = object : ViewGroupContext.ProjectChangeListener(this) {
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
