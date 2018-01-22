/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application.task

import javafx.concurrent.Task
import javafx.concurrent.WorkerStateEvent

/**
 * Wrapper for [Task] for use in LTTng-Scope, the task will be registered to the
 * central [ScopeTaskManager], which will allow outputs to report progress on these
 * tasks.
 *
 * Override execute() instead of call().
 *
 * Note: We extend Task<Void?> (and return null) instead of Task<Unit>, for compatibility
 * with Java implementations.
 */
abstract class ScopeTask(taskTitle: String?) : Task<Void?>() {

    init {
        taskTitle?.let { updateTitle(it) }
        updateProgress(-1.0, 0.0)
    }

    /* Make this "final" so we can safely call it from the constructor. */
    final override fun updateProgress(workDone: Double, max: Double) {
        super.updateProgress(workDone, max)
    }

    protected abstract fun execute()

    /**
     * Reserve the call() function for our needs, have sub-classes implement execute() instead.
     */
    final override fun call(): Void? {
        ScopeTaskManager.registerTask(this)
        try {
            execute()
        } finally {
            ScopeTaskManager.deregisterTask(this)
        }
        return null
    }
}
