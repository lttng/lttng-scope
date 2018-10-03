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
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue
import com.efficios.jabberwocky.lttng.kernel.analysis.os.Attributes
import com.efficios.jabberwocky.lttng.kernel.analysis.os.LinuxValues
import com.efficios.jabberwocky.lttng.kernel.analysis.os.StateValues
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngKernelEventLayout
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue
import com.efficios.jabberwocky.trace.event.FieldValue.StringValue
import com.efficios.jabberwocky.trace.event.TraceEvent

class SchedSwitchHandler(layout: LttngKernelEventLayout) : KernelEventHandler(layout) {

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent) {
        val cpu = event.cpu
        val timestamp = event.timestamp

        val prevProcessName = (event.fields[layout.fieldPrevComm] as StringValue).value
        val prevTid = (event.fields[layout.fieldPrevTid] as IntegerValue).value.toInt()
        val prevState = (event.fields[layout.fieldPrevState] as IntegerValue).value.toInt()
        val prevPrio = (event.fields[layout.fieldPrevPrio] as IntegerValue).value.toInt()
        val nextProcessName = (event.fields[layout.fieldNextComm] as StringValue).value
        val nextTid = (event.fields[layout.fieldNextTid] as IntegerValue).value.toInt()
        val nextPrio = (event.fields[layout.fieldNextPrio] as IntegerValue).value.toInt()

        /* Will never return null since "cpu" is never null here. */
        val formerThreadAttributeName = Attributes.buildThreadAttributeName(prevTid, cpu)!!
        val currentThreadAttributeName = Attributes.buildThreadAttributeName(nextTid, cpu)!!

        val nodeThreads = ss.getNodeThreads()
        val formerThreadNode = ss.getQuarkRelativeAndAdd(nodeThreads, formerThreadAttributeName)
        val newCurrentThreadNode = ss.getQuarkRelativeAndAdd(nodeThreads, currentThreadAttributeName)

        /*
         * Set the status of the process that got scheduled out. This will also
         * set it's current CPU run queue accordingly.
         */
        ss.setOldProcessStatus(prevState, formerThreadNode, cpu, timestamp)

        /* Set the status of the new scheduled process */
        ss.setProcessToRunning(timestamp, newCurrentThreadNode)

        /*
         * Set the current CPU run queue of the new process. Should be already
         * set if we've seen the previous sched_wakeup, but doesn't hurt to set
         * it here too.
         */
        ss.modifyAttribute(timestamp,
                StateValue.newValueInt(cpu),
                ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.CURRENT_CPU_RQ))

        /* Set the exec name of the former process */
        ss.setProcessExecName(prevProcessName, formerThreadNode, timestamp)

        /* Set the exec name of the new process */
        ss.setProcessExecName(nextProcessName, newCurrentThreadNode, timestamp)

        /* Set the current prio for the former process */
        ss.setProcessPrio(prevPrio, formerThreadNode, timestamp)

        /* Set the current prio for the new process */
        ss.setProcessPrio(nextPrio, newCurrentThreadNode, timestamp)

        /* Set the current scheduled process on the relevant CPU */
        val cpuNode = ss.getCPUNode(cpu)
        ss.setCpuProcess(nextTid, timestamp, cpuNode)

        /* Set the status of the CPU itself */
        ss.setCpuStatus(nextTid, newCurrentThreadNode, timestamp, cpuNode)
    }
}

private fun IStateSystemWriter.setOldProcessStatus(prevState: Int, formerThreadNode: Int, cpu: Int, timestamp: Long) {

    fun isDead(state: Int): Boolean =
            (state and LinuxValues.TASK_DEAD) != 0

    fun isWaiting(state: Int): Boolean =
            (state and (LinuxValues.TASK_INTERRUPTIBLE or LinuxValues.TASK_UNINTERRUPTIBLE)) != 0

    fun isRunning(state: Int): Boolean {
        // special case, this means ALL STATES ARE 0
        // this is effectively an anti-state
        return state == 0
    }

    var staysOnRunQueue = false
    /*
     * Empirical observations and look into the linux code have
     * shown that the TASK_STATE_MAX flag is used internally and
     * |'ed with other states, most often the running state, so it
     * is ignored from the prevState value.
     *
     * Since Linux 4.1, the TASK_NOLOAD state was created and
     * TASK_STATE_MAX is now 2048. We use TASK_NOLOAD as the new max
     * because it does not modify the displayed state value.
     */
    val state = (prevState and (LinuxValues.TASK_NOLOAD - 1))

    when {
        isRunning(state) -> {
            staysOnRunQueue = true
            StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE
        }
        isWaiting(state) -> StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE
        isDead(state) -> StateValue.nullValue()
        else -> StateValues.PROCESS_STATUS_WAIT_UNKNOWN_VALUE
    }
            .let { sv -> modifyAttribute(timestamp, sv, formerThreadNode) }

    val sv = if (staysOnRunQueue) {
        /*
         * Set the thread's run queue. This will often be redundant with
         * previous events, but it may be the first time we see the
         * information too.
         */
        StateValue.newValueInt(cpu)
    } else {
        StateValue.nullValue()
    }
    modifyAttribute(timestamp,
            sv,
            getQuarkRelativeAndAdd(formerThreadNode, Attributes.CURRENT_CPU_RQ))

}

private fun IStateSystemWriter.setCpuStatus(nextTid: Int, newCurrentThreadNode: Int, timestamp: Long, currentCPUNode: Int) {
    val value = if (nextTid > 0) {
        /* Check if the entering process is in kernel or user mode */
        val ongoingState = getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.SYSTEM_CALL)
                .let { queryOngoingState(it) }
        if (ongoingState.isNull) {
            StateValues.CPU_STATUS_RUN_USERMODE_VALUE
        } else {
            StateValues.CPU_STATUS_RUN_SYSCALL_VALUE
        }
    } else {
        StateValues.CPU_STATUS_IDLE_VALUE
    }
    modifyAttribute(timestamp, value, currentCPUNode)
}

private fun IStateSystemWriter.setCpuProcess(nextTid: Int, timestamp: Long, currentCPUNode: Int) =
        modifyAttribute(timestamp,
                StateValue.newValueInt(nextTid),
                getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD))

private fun IStateSystemWriter.setProcessPrio(prio: Int, threadNode: Int, timestamp: Long) =
        modifyAttribute(timestamp,
                StateValue.newValueInt(prio),
                getQuarkRelativeAndAdd(threadNode, Attributes.PRIO))

private fun IStateSystemWriter.setProcessExecName(processName: String, threadNode: Int, timestamp: Long) =
        modifyAttribute(timestamp,
                StateValue.newValueString(processName),
                getQuarkRelativeAndAdd(threadNode, Attributes.EXEC_NAME))

