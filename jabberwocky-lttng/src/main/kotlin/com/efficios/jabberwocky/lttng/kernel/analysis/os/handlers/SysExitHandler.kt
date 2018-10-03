/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.analysis.os.handlers;

import ca.polymtl.dorsal.libdelorean.IStateSystemWriter
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue
import com.efficios.jabberwocky.lttng.kernel.analysis.os.Attributes
import com.efficios.jabberwocky.lttng.kernel.analysis.os.StateValues
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngKernelEventLayout
import com.efficios.jabberwocky.trace.event.TraceEvent

class SysExitHandler(layout: LttngKernelEventLayout) : KernelEventHandler(layout) {

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent) {
        val cpu = event.cpu
        val timestamp = event.timestamp

        ss.getCurrentThreadNode(cpu)?.let { threadNode ->
            /* Assign the new system call to the process */
            ss.modifyAttribute(timestamp,
                    StateValue.nullValue(),
                    ss.getQuarkRelativeAndAdd(threadNode, Attributes.SYSTEM_CALL))

            /* Put the process in system call mode */
            ss.modifyAttribute(timestamp,
                    StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE,
                    threadNode)
        }

        /* Put the CPU in system call (kernel) mode */
        ss.modifyAttribute(timestamp,
                StateValues.CPU_STATUS_RUN_USERMODE_VALUE,
                ss.getCPUNode(cpu))
    }

}
