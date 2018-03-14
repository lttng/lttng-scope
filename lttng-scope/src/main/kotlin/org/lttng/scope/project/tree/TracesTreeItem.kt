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
import javafx.scene.control.TreeItem

internal class TracesTreeItem : ProjectTreeItem(TRACES_NODE_NAME) {

    companion object {
        private const val TRACES_NODE_NAME = "Traces"
    }

    override fun initForProject(project: TraceProject<*, *>) {
        val traces = project.traceCollections.flatMap { it.traces }
        val newChildren = traces.map { TreeItem(it.name) }

        children.clear()
        children.addAll(newChildren)
    }
}
