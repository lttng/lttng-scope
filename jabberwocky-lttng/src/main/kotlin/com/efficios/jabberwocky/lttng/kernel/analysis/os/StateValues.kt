/*
 * Copyright (C) 2015 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.analysis.os

import ca.polymtl.dorsal.libdelorean.statevalue.StateValue

/**
 * State values that are used in the kernel event handler. It's much better to
 * use integer values whenever possible, since those take much less space in the
 * history file.
 */
object StateValues {

    /* Process status */
    const val PROCESS_STATUS_UNKNOWN = 0
    const val PROCESS_STATUS_WAIT_BLOCKED = 1
    const val PROCESS_STATUS_RUN_USERMODE = 2
    const val PROCESS_STATUS_RUN_SYSCALL = 3
    const val PROCESS_STATUS_INTERRUPTED = 4
    const val PROCESS_STATUS_WAIT_FOR_CPU = 5
    const val PROCESS_STATUS_WAIT_UNKNOWN = 6

    @JvmField val PROCESS_STATUS_UNKNOWN_VALUE = StateValue.newValueInt(PROCESS_STATUS_UNKNOWN)
    @JvmField val PROCESS_STATUS_WAIT_UNKNOWN_VALUE = StateValue.newValueInt(PROCESS_STATUS_WAIT_UNKNOWN)
    @JvmField val PROCESS_STATUS_WAIT_BLOCKED_VALUE = StateValue.newValueInt(PROCESS_STATUS_WAIT_BLOCKED)
    @JvmField val PROCESS_STATUS_RUN_USERMODE_VALUE = StateValue.newValueInt(PROCESS_STATUS_RUN_USERMODE)
    @JvmField val PROCESS_STATUS_RUN_SYSCALL_VALUE = StateValue.newValueInt(PROCESS_STATUS_RUN_SYSCALL)
    @JvmField val PROCESS_STATUS_INTERRUPTED_VALUE = StateValue.newValueInt(PROCESS_STATUS_INTERRUPTED)
    @JvmField val PROCESS_STATUS_WAIT_FOR_CPU_VALUE = StateValue.newValueInt(PROCESS_STATUS_WAIT_FOR_CPU)

    /* CPU Status */
    const val CPU_STATUS_IDLE = 0
    /**
     * Soft IRQ raised, could happen in the CPU attribute but should not since
     * this means that the CPU went idle when a softirq was raised.
     */
    const val CPU_STATUS_SOFT_IRQ_RAISED = 1
    const val CPU_STATUS_RUN_USERMODE = (1 shl 1)
    const val CPU_STATUS_RUN_SYSCALL = (1 shl 2)
    const val CPU_STATUS_SOFTIRQ = (1 shl 3)
    const val CPU_STATUS_IRQ = (1 shl 4)

    @JvmField val CPU_STATUS_IDLE_VALUE = StateValue.newValueInt(CPU_STATUS_IDLE)
    @JvmField val CPU_STATUS_RUN_USERMODE_VALUE = StateValue.newValueInt(CPU_STATUS_RUN_USERMODE)
    @JvmField val CPU_STATUS_RUN_SYSCALL_VALUE = StateValue.newValueInt(CPU_STATUS_RUN_SYSCALL)
    @JvmField val CPU_STATUS_IRQ_VALUE = StateValue.newValueInt(CPU_STATUS_IRQ)
    @JvmField val CPU_STATUS_SOFTIRQ_VALUE = StateValue.newValueInt(CPU_STATUS_SOFTIRQ)

    /** Soft IRQ is raised, CPU is in user mode */
    @JvmField val SOFT_IRQ_RAISED_VALUE = StateValue.newValueInt(CPU_STATUS_SOFT_IRQ_RAISED)

    /** If the softirq is running and another is raised at the same time. */
    @JvmField val SOFT_IRQ_RAISED_RUNNING_VALUE = StateValue.newValueInt(CPU_STATUS_SOFT_IRQ_RAISED or CPU_STATUS_SOFTIRQ)
}
