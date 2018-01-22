/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application.task

import javafx.application.Platform
import org.controlsfx.control.TaskProgressView

class ScopeTaskProgressView : TaskProgressView<ScopeTask>(), ScopeTaskManager.TaskManagerOutput {

    override fun taskRegistered(task: ScopeTask) {
        Platform.runLater { tasks.add(task) }
    }

    override fun taskDeregistered(task: ScopeTask) {
        Platform.runLater { tasks.remove(task) }
    }

}
