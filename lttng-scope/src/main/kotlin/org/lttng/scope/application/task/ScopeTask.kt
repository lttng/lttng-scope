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
import java.util.concurrent.atomic.AtomicInteger

/**
 * Wrapper for [Task] for use in LTTng-Scope, the task will be registered to the
 * central [ScopeTaskManager], which will allow outputs to report progress on these
 * tasks.
 *
 * The second constructor parameter is the lambda to execute asynhcronously. The parameter
 * to this labmda is the Task itself, so that 'if (it.isCancelled())' can be used for example.
 *
 * Note: We extend Task<Void?> (and return null) instead of Task<Unit>, for compatibility
 * with Java implementations.
 */
class ScopeTask<R>(taskTitle: String?,
                   private val block: (ScopeTask<R>) -> R) : Task<R>() {

    companion object {
        private val seqNumCounter = AtomicInteger(0)
    }

    val taskSeqNum = seqNumCounter.getAndIncrement()

    init {
        taskTitle?.let { updateTitle(it) }
        updateProgress(-1.0, 0.0)
    }

    /* Override to make public, so it's accessible via the "it" parameter. */
    public override fun updateTitle(title: String?) = super.updateTitle(title)

    /**
     * Reserve the call() function for our needs, have sub-classes pass the code block at the constructor.
     */
    override fun call(): R {
        ScopeTaskManager.registerTask(this)
        try {
            return block(this)
        } finally {
            ScopeTaskManager.deregisterTask(this)
        }
    }
}
