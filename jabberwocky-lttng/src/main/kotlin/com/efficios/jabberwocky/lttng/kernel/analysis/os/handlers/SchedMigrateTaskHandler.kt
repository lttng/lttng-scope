/*
 * Copyright (C) 2016-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
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
import com.efficios.jabberwocky.trace.event.TraceEvent

/**
 * Handler for task migration events. Normally moves a (non-running) process
 * from one run queue to another.
 */
class SchedMigrateTaskHandler(layout: LttngKernelEventLayout) : KernelEventHandler(layout) {

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent) {
        val t = event.timestamp
        val tid = (event.fields[layout.fieldTid] as? IntegerValue)?.value?.toInt() ?: return
        val destCpu = (event.fields[layout.fieldDestCpu] as? IntegerValue)?.value?.toInt() ?: return

        val threadAttributeName = Attributes.buildThreadAttributeName(tid, null)
                ?: /* Swapper threads do not get migrated */
                return
        val threadNode = ss.getQuarkRelativeAndAdd(ss.getNodeThreads(), threadAttributeName)

        /*
         * Put the thread in the "wait for cpu" state. Some older versions of
         * the kernel/tracers may not have the corresponding sched_waking events
         * that also does so, so we can set it at the migrate, if applicable.
         */
        ss.modifyAttribute(t,
                StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE,
                threadNode)

        /* Update the thread's running queue to the new one indicated by the event */
        ss.modifyAttribute(t,
                StateValue.newValueInt(destCpu),
                ss.getQuarkRelativeAndAdd(threadNode, Attributes.CURRENT_CPU_RQ))
    }

}
