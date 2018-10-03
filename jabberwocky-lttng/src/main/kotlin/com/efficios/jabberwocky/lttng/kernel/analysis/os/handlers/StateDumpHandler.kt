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

/**
 * LTTng-specific state dump event handler
 */
class StateDumpHandler(layout: LttngKernelEventLayout) : KernelEventHandler(layout) {

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent) {
        val eventCpu = event.cpu
        val timestamp = event.timestamp

        val tid = (event.fields["tid"] as IntegerValue).value.toInt()
        val pid = (event.fields["pid"] as IntegerValue).value.toInt()
        val ppid = (event.fields["ppid"] as IntegerValue).value.toInt()
        val status = (event.fields["status"] as IntegerValue).value.toInt()
        val name = (event.fields["name"] as StringValue).value

        /* Only present in LTTng 2.10+ */
        val cpuField = (event.fields["cpu"] as? IntegerValue)?.value?.toInt()

        /*
         * "mode" could be interesting too, but it doesn't seem to be populated
         * with anything relevant for now.
         */

        val threadAttributeName = Attributes.buildThreadAttributeName(tid, eventCpu) ?: return
        val curThreadNode = ss.getQuarkRelativeAndAdd(ss.getNodeThreads(), threadAttributeName)

        /* Set the process' name */
        ss.setProcessName(name, curThreadNode, timestamp)

        /* Set the process' PPID */
        ss.setPpid(tid, pid, ppid, curThreadNode, timestamp)

        /* Set the process' status */
        ss.setStatus(status, curThreadNode, cpuField, timestamp)
    }
}

private fun IStateSystemWriter.setStatus(status: Int, curThreadNode: Int, cpu: Int?, timestamp: Long) {
    if (!queryOngoingState(curThreadNode).isNull) return

    val value = when (status) {
        LinuxValues.STATEDUMP_PROCESS_STATUS_WAIT_CPU -> {
            setRunQueue(curThreadNode, cpu, timestamp)
            StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE
        }
        LinuxValues.STATEDUMP_PROCESS_STATUS_WAIT -> {
            /*
         * We have no information on what the process is waiting on
         * (unlike a sched_switch for example), so we will use the
         * WAIT_UNKNOWN state instead of the "normal" WAIT_BLOCKED
         * state.
         */
            StateValues.PROCESS_STATUS_WAIT_UNKNOWN_VALUE
        }
        else -> StateValues.PROCESS_STATUS_UNKNOWN_VALUE
    }
    modifyAttribute(timestamp, value, curThreadNode)
}

private fun IStateSystemWriter.setRunQueue(curThreadNode: Int, cpu: Int?, timestamp: Long) {
    cpu ?: return
    modifyAttribute(timestamp,
            StateValue.newValueInt(cpu),
            getQuarkRelativeAndAdd(curThreadNode, Attributes.CURRENT_CPU_RQ))
}

private fun IStateSystemWriter.setPpid(tid: Int, pid: Int, ppid: Int, curThreadNode: Int, timestamp: Long) {
    val quark = getQuarkRelativeAndAdd(curThreadNode, Attributes.PPID)
    if (!queryOngoingState(quark).isNull) return

    if (pid == tid) {
        /* We have a process. Use the 'PPID' field. */
        StateValue.newValueInt(ppid)
    } else {
        /* We have a thread, use the 'PID' field for the parent. */
        StateValue.newValueInt(pid)
    }.let { sv -> modifyAttribute(timestamp, sv, quark) }

}

private fun IStateSystemWriter.setProcessName(name: String, curThreadNode: Int, timestamp: Long) {
    val quark = getQuarkRelativeAndAdd(curThreadNode, Attributes.EXEC_NAME)
    if (!queryOngoingState(quark).isNull) return

    /* If the value didn't exist previously, set it */
    modifyAttribute(timestamp, StateValue.newValueString(name), quark)
}
