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
import com.efficios.jabberwocky.lttng.kernel.analysis.os.StateValues
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngKernelEventLayout
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue
import com.efficios.jabberwocky.trace.event.FieldValue.StringValue
import com.efficios.jabberwocky.trace.event.TraceEvent

class ProcessForkHandler(layout: LttngKernelEventLayout) : KernelEventHandler(layout) {

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent) {
        val cpu = event.cpu
        val timestamp = event.timestamp

        val childProcessName = (event.fields[layout.fieldChildComm] as StringValue).value
        val parentTid = (event.fields[layout.fieldParentTid] as IntegerValue).value.toInt()
        val childTid = (event.fields[layout.fieldChildTid] as IntegerValue).value.toInt()

        val parentThreadAttributeName = Attributes.buildThreadAttributeName(parentTid, cpu) ?: return
        val childThreadAttributeName = Attributes.buildThreadAttributeName(childTid, cpu) ?: return

        val threadsNode = ss.getNodeThreads()
        val parentTidNode = ss.getQuarkRelativeAndAdd(threadsNode, parentThreadAttributeName)
        val childTidNode = ss.getQuarkRelativeAndAdd(threadsNode, childThreadAttributeName)


        /* Assign the PPID to the new process */
        ss.modifyAttribute(timestamp,
                StateValue.newValueInt(parentTid),
                ss.getQuarkRelativeAndAdd(childTidNode, Attributes.PPID))

        /* Set the new process' exec_name */
        ss.modifyAttribute(timestamp,
                StateValue.newValueString(childProcessName),
                ss.getQuarkRelativeAndAdd(childTidNode, Attributes.EXEC_NAME))

        /*
         * Set the new process' status, it is initially in a blocked state. A
         * subsequent sched_wakeup_new will schedule it.
         */
        ss.modifyAttribute(timestamp,
                StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE,
                childTidNode)

        /* Set the process' syscall name, to be the same as the parent's */
        ss.getQuarkRelativeAndAdd(parentTidNode, Attributes.SYSTEM_CALL)
                .let { quark -> ss.queryOngoingState(quark) }
                .let { sv ->
                    if (!sv.isNull) {
                        ss.modifyAttribute(timestamp,
                                sv,
                                ss.getQuarkRelativeAndAdd(childTidNode, Attributes.SYSTEM_CALL))
                    }
                }

    }
}
