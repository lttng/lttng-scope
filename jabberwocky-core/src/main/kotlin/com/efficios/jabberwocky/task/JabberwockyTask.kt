/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.task

import javafx.concurrent.Task
import java.util.concurrent.atomic.AtomicInteger

/**
 * Wrapper for [Task] which will be automatically registered to the central
 * [JabberwockyTaskManager], which will allow outputs to report progress on these
 * tasks.
 *
 * The second constructor parameter is the lambda to execute asynchronously. The parameter
 * to this lambda is the Task itself, so that 'if (it.isCancelled())' can be used for example.
 */
class JabberwockyTask<R>(taskTitle: String?,
                         private val block: (JabberwockyTask<R>) -> R) : Task<R>() {

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
        JabberwockyTaskManager.registerTask(this)
        try {
            return block(this)
        } finally {
            JabberwockyTaskManager.deregisterTask(this)
        }
    }
}
