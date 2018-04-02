/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.project

import javafx.scene.layout.BorderPane
import org.lttng.scope.project.tree.ProjectTree

class ProjectArea : BorderPane() {

    private val projectTree = ProjectTree()

    init {
        center = projectTree
    }
}
