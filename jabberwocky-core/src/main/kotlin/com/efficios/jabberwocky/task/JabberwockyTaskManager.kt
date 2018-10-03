/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.task

object JabberwockyTaskManager {

    interface TaskManagerOutput {
        fun taskRegistered(task: JabberwockyTask<*>)
        fun taskDeregistered(task: JabberwockyTask<*>)
    }

    private val registeredOutputs = mutableSetOf<TaskManagerOutput>()
    private val registeredTasks = mutableSetOf<JabberwockyTask<*>>()

    @Synchronized
    fun registerOutput(output: TaskManagerOutput) {
        /* Send notifications for currently-registered tasks */
        registeredTasks.forEach { output.taskRegistered(it) }
        /* Register the output */
        registeredOutputs.add(output)
    }

    @Synchronized
    fun registerTask(task: JabberwockyTask<*>) {
        val added = registeredTasks.add(task)
        /* Emit notifications to the registered outputs */
        if (added) registeredOutputs.forEach { it.taskRegistered(task) }
    }

    @Synchronized
    fun deregisterTask(task: JabberwockyTask<*>) {
        val removed = registeredTasks.remove(task)
        /* Emit notifications to the registered outputs */
        if (removed) registeredOutputs.forEach { it.taskDeregistered(task) }
    }

    /**
     * Specific for the status bar. Could be cleaner...
     */
    @Synchronized
    fun getOldestTask(): JabberwockyTask<*>? = registeredTasks.firstOrNull()

    @Synchronized
    fun getLatestTask(): JabberwockyTask<*>? = registeredTasks.lastOrNull()
}
