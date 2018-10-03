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
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngKernelEventLayout
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue
import com.efficios.jabberwocky.trace.event.TraceEvent

/**
 * Waking/wakeup handler.
 * <p>
 * "sched_waking" and "sched_wakeup" tracepoints contain the same fields, and
 * apply the same state transitions in our model, so they can both use this
 * handler.
 */
class SchedWakeupHandler(layout: LttngKernelEventLayout) : KernelEventHandler(layout) {

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent) {
        val cpu = event.cpu
        val timestamp = event.timestamp

        val tid = (event.fields[layout.fieldTid] as IntegerValue).value.toInt()
        val prio = (event.fields[layout.fieldPrio] as IntegerValue).value.toInt()
        val targetCpu = (event.fields[layout.fieldTargetCpu] as IntegerValue).value.toInt()

        val threadAttributeName = Attributes.buildThreadAttributeName(tid, cpu) ?: return
        val threadNode = ss.getQuarkRelativeAndAdd(ss.getNodeThreads(), threadAttributeName)

        /*
         * The process indicated in the event's payload is now ready to run.
         * Assign it to the "wait for cpu" state, but only if it was not already
         * running.
         */
        val ongoingSv = ss.queryOngoingState(threadNode)
        val status = (ongoingSv as? IntegerStateValue)?.value ?: -1
        if (status != StateValues.PROCESS_STATUS_RUN_SYSCALL && status != StateValues.PROCESS_STATUS_RUN_USERMODE) {
            ss.modifyAttribute(timestamp,
                    StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE,
                    threadNode)
        }

        /* Set the thread's target run queue */
        ss.modifyAttribute(timestamp,
                StateValue.newValueInt(targetCpu),
                ss.getQuarkRelativeAndAdd(threadNode, Attributes.CURRENT_CPU_RQ))

        /*
         * When a user changes a threads prio (e.g. with pthread_setschedparam),
         * it shows in ftrace with a sched_wakeup.
         */
        ss.modifyAttribute(timestamp,
                StateValue.newValueInt(prio),
                ss.getQuarkRelativeAndAdd(threadNode, Attributes.PRIO))
    }
}
