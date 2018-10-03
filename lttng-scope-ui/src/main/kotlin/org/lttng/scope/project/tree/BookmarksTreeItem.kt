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

internal class BookmarksTreeItem : ProjectTreeItem(BOOKMARKS_NODE_NAME) {

    companion object {
        private const val BOOKMARKS_NODE_NAME = "Bookmarks"
    }

    override fun initForProject(project: TraceProject<*, *>) {
        // TODO
    }
}
