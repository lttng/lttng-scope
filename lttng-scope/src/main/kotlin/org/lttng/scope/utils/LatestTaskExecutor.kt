/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.utils

import javafx.concurrent.Task
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

class LatestTaskExecutor {

    private val executor = Executors.newFixedThreadPool(2)
//    private val executor = Executors.newSingleThreadExecutor()

    /** The latest job that was scheduled in this queue. */
    private var latestTask = WeakReference<Task<*>>(null)

    @Synchronized
    fun schedule(newTask: Task<*>) {
        /* Cancel the existing task. Here's hoping it cooperates and ends quickly! */
        latestTask.get()?.cancel(false)

        /* Start the new job */
        latestTask = WeakReference(newTask)
        executor.submit(newTask)
    }

}
