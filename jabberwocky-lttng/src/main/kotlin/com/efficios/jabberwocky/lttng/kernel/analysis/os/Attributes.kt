/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2012-2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.analysis.os

/**
 * This file defines all the attribute names used in the handler. Both the
 * construction and query steps should use them.
 *
 * These should not be externalized! The values here are used as-is in the
 * history file on disk, so they should be kept the same to keep the file format
 * compatible. If a view shows attribute names directly, the localization should
 * be done on the viewer side.
 */
object Attributes {

    /* First-level attributes */
    const val CPUS = "CPUs"
    const val THREADS = "Threads"

    /* Sub-attributes of the CPU nodes */
    const val CURRENT_THREAD = "Current_thread"
    const val SOFT_IRQS = "Soft_IRQs"
    const val IRQS = "IRQs"

    /* Sub-attributes of the Thread nodes */
    const val CURRENT_CPU_RQ = "Current_cpu_rq"
    const val PPID = "PPID"
    const val EXEC_NAME = "Exec_name"

    const val PRIO = "Prio"
    const val SYSTEM_CALL = "System_call"

    /* Misc stuff */
    const val UNKNOWN = "Unknown"
    const val THREAD_0_PREFIX = "0_"
    const val THREAD_0_SEPARATOR = "_"

    /**
     * Build the thread attribute name.
     *
     * For all threads except "0" this is the string representation of the
     * threadId. For thread "0" which is the idle thread and can be running
     * concurrently on multiple CPUs, append "_cpuId".
     *
     * @param threadId
     *            the thread id
     * @param cpuId
     *            the cpu id
     * @return the thread attribute name null if the threadId is zero and the
     *         cpuId is null
     */
    @JvmStatic
    fun buildThreadAttributeName(threadId: Int, cpuId: Int?): String? {
        if (threadId == 0) {
            cpuId ?: return null
            return "${Attributes.THREAD_0_PREFIX}$cpuId"
        }

        return threadId.toString()
    }

    /**
     * Parse the thread id and CPU id from the thread attribute name string
     *
     * For thread "0" the attribute name is in the form "threadId_cpuId",
     * extract both values from the string.
     *
     * For all other threads, the attribute name is the string representation of
     * the threadId and there is no cpuId.
     *
     * @param threadAttributeName
     *            the thread attribute name
     * @return the thread id and cpu id
     */
    @JvmStatic
    fun parseThreadAttributeName(threadAttributeName: String): Pair<Int, Int> {
        var threadId = -1
        var cpuId = -1

        try {
            if (threadAttributeName.startsWith(Attributes.THREAD_0_PREFIX)) {
                threadId = 0
                val tokens = threadAttributeName.split(Attributes.THREAD_0_SEPARATOR)
                cpuId = Integer.parseInt(tokens[1])
            } else {
                threadId = Integer.parseInt(threadAttributeName)
            }
        } catch (e: NumberFormatException) {
            // ignore
        }

        return (threadId to cpuId)
    }

}
