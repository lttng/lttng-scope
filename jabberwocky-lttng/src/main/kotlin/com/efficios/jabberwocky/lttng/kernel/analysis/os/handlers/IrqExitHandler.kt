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
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngKernelEventLayout
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue
import com.efficios.jabberwocky.trace.event.TraceEvent

class IrqExitHandler(layout: LttngKernelEventLayout) : KernelEventHandler(layout) {

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent) {
        val cpu = event.cpu
        val timestamp = event.timestamp
        val irqId = (event.fields[layout.fieldIrq] as IntegerValue).value

        /* Put this IRQ back to inactive in the resource tree */
        ss.modifyAttribute(timestamp,
                StateValue.nullValue(),
                ss.getQuarkRelativeAndAdd(ss.getNodeIRQs(cpu), irqId.toString()))

        /* Set the previous process back to running */
        ss.getCurrentThreadNode(cpu)?.let { ss.setProcessToRunning(timestamp, it) }

        /* Set the CPU status back to running or "idle" */
        ss.cpuExitInterrupt(timestamp, cpu)
    }
}
