/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.analysis.os.handlers

import ca.polymtl.dorsal.libdelorean.IStateSystemWriter
import ca.polymtl.dorsal.libdelorean.statevalue.IntegerStateValue
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue
import com.efficios.jabberwocky.lttng.kernel.analysis.os.Attributes
import com.efficios.jabberwocky.lttng.kernel.analysis.os.StateValues

/**
 * Kernel Event Handler Utils is a collection of static methods to be used in
 * subclasses of IKernelEventHandler.
 */

/**
 * Gets the CPU quark of the given CPU.
 */
fun IStateSystemWriter.getCPUNode(cpuNumber: Int): Int =
        getQuarkRelativeAndAdd(getNodeCPUs(), cpuNumber.toString())

/**
 * Get the node quark of the thread currently running on the given CPU.
 * 'null' is returned if we do not currently know which thread is running on this cpu.
 */
fun IStateSystemWriter.getCurrentThreadNode(cpuNumber: Int): Int? {
    /*
     * Shortcut for the "current thread" attribute node. It requires
     * querying the current CPU's current thread.
     */
    val quark = getQuarkRelativeAndAdd(getCPUNode(cpuNumber), Attributes.CURRENT_THREAD)
    val value = queryOngoingState(quark)
    val thread = (value as? IntegerStateValue)?.value ?: return null
    return getQuarkRelativeAndAdd(getNodeThreads(), Attributes.buildThreadAttributeName(thread, cpuNumber))
}

/**
 * When we want to set a process back to a "running" state, first check its
 * current System_call attribute. If there is a system call active, we put
 * the process back in the syscall state. If not, we put it back in user
 * mode state.
 */
fun IStateSystemWriter.setProcessToRunning(timestamp: Long, currentThreadNode: Int) {
    val quark = getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL)
    val sv = if (queryOngoingState(quark).isNull) {
        /* We were in user mode before the interruption */
        StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE
    } else {
        /* We were previously in kernel mode */
        StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE
    }
    modifyAttribute(timestamp, sv, currentThreadNode)
}

/**
 * Get the "IRQs" node for the given CPU.
 */
fun IStateSystemWriter.getNodeIRQs(cpuNumber: Int): Int =
        getQuarkAbsoluteAndAdd(Attributes.CPUS, Integer.toString(cpuNumber), Attributes.IRQS)

/**
 * Get the "CPUs" node.
 */
fun IStateSystemWriter.getNodeCPUs(): Int =
        getQuarkAbsoluteAndAdd(Attributes.CPUS)

/**
 * Get the Soft IRQs node for the given CPU.
 */
fun IStateSystemWriter.getNodeSoftIRQs(cpuNumber: Int): Int =
        getQuarkAbsoluteAndAdd(Attributes.CPUS, Integer.toString(cpuNumber), Attributes.SOFT_IRQS)

/**
 * Get the "Threads" node.
 */
fun IStateSystemWriter.getNodeThreads(): Int =
        getQuarkAbsoluteAndAdd(Attributes.THREADS)

/**
 * Reset the CPU's status when it's coming out of an interruption.
 */
fun IStateSystemWriter.cpuExitInterrupt(timestamp: Long, cpuNumber: Int) {
    val cpuNode = getCPUNode(cpuNumber)
    val value = getCpuStatus(cpuNode)
    modifyAttribute(timestamp, value, cpuNode)
}

/**
 * Get the ongoing Status state of a CPU.
 *
 * This will look through the states of the
 *
 * <ul>
 * <li>IRQ</li>
 * <li>Soft IRQ</li>
 * <li>Process</li>
 * </ul>
 *
 * under the CPU, giving priority to states higher in the list. If a state
 * is a null value, we continue looking down the list.
 *
 * @param cpuQuark
 *            The *quark* of the CPU we are looking for. Careful, this is
 *            NOT the CPU number (or attribute name)!
 * @return The state value that represents the status of the given CPU
 */
private fun IStateSystemWriter.getCpuStatus(cpuQuark: Int): StateValue {

    /* Check if there is a IRQ running */
    getQuarkRelativeAndAdd(cpuQuark, Attributes.IRQS)
            .let { getSubAttributes(it, false) }
            .map { queryOngoingState(it) }
            .forEach {
                if (!it.isNull) {
                    return it
                }
            }

    /* Check if there is a soft IRQ running */
    getQuarkRelativeAndAdd(cpuQuark, Attributes.SOFT_IRQS)
            .let { getSubAttributes(it, false) }
            .map { queryOngoingState(it) }
            .forEach {
                if (!it.isNull) {
                    return it
                }
            }

    /*
     * Check if there is a thread running. If not, report IDLE. If there is,
     * report the running state of the thread (usermode or system call).
     */
    val currentThreadState = getQuarkRelativeAndAdd(cpuQuark, Attributes.CURRENT_THREAD)
            .let { queryOngoingState(it) } as? IntegerStateValue ?: return StateValue.nullValue()

    val tid = currentThreadState.value
    if (tid == 0) {
        return StateValues.CPU_STATUS_IDLE_VALUE
    }
    val threadSystemCallQuark = getQuarkAbsoluteAndAdd(Attributes.THREADS, tid.toString(), Attributes.SYSTEM_CALL)
    return if (queryOngoingState(threadSystemCallQuark).isNull) {
        StateValues.CPU_STATUS_RUN_USERMODE_VALUE
    } else {
        StateValues.CPU_STATUS_RUN_SYSCALL_VALUE
    }
}
