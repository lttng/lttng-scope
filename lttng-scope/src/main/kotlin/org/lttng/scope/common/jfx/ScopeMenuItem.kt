/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common.jfx

import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.project.TraceProject
import javafx.event.ActionEvent
import javafx.scene.control.MenuItem
import org.lttng.scope.views.context.ViewGroupContextManager

/**
 * Specification of [MenuItem] which allow defining the onAction more easily, as well as
 * a simple flag to define it this option should be disabled if there is currently no
 * active project.
 */
class ScopeMenuItem(label: String,
                    enableOnlyOnActiveProject: Boolean = false,
                    onAction: (ActionEvent) -> Unit) : MenuItem(label) {

    private val projectChangeListener = if (enableOnlyOnActiveProject) {
        object : ViewGroupContext.ProjectChangeListener(this) {
            override fun newProjectCb(newProject: TraceProject<*, *>?) {
                isDisable = (newProject == null)
            }
        }
    } else {
        null
    }

    init {
        setOnAction(onAction)

        val ctx = ViewGroupContextManager.getCurrent()
        if (enableOnlyOnActiveProject && ctx.traceProject == null) this.isDisable = true
        projectChangeListener?.let { ctx.registerProjectChangeListener(it) }
    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        projectChangeListener?.let { ViewGroupContextManager.getCurrent().deregisterProjectChangeListener(it) }
    }
}
