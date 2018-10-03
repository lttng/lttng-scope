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
import com.efficios.jabberwocky.lttng.kernel.analysis.os.StateValues
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngKernelEventLayout
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue
import com.efficios.jabberwocky.trace.event.TraceEvent

class SoftIrqExitHandler(layout: LttngKernelEventLayout) : KernelEventHandler(layout) {

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent) {
        val cpu = event.cpu
        val timestamp = event.timestamp

        val softIrqId = (event.fields[layout.fieldVec] as IntegerValue).value.toInt()

        /* Put this SoftIRQ back to inactive (= -1) in the resource tree */
        val quark = ss.getQuarkRelativeAndAdd(ss.getNodeSoftIRQs(cpu), softIrqId.toString())
        if (isSoftIrqRaised(ss.queryOngoingState(quark))) {
            ss.modifyAttribute(timestamp, StateValues.SOFT_IRQ_RAISED_VALUE, quark)
        } else {
            ss.modifyAttribute(timestamp, StateValue.nullValue(), quark)
        }

        ss.getSubAttributes(ss.getParentAttributeQuark(quark), false)
                /* Only set status to running and no exit if ALL softirqs are exited. */
                .forEach { softIrqQuark ->
                    if (!ss.queryOngoingState(softIrqQuark).isNull) {
                        return
                    }
                }

        /* Set the previous process back to running */
        ss.getCurrentThreadNode(cpu)?.let { ss.setProcessToRunning(timestamp, it) }

        /* Set the CPU status back to "busy" or "idle" */
        ss.cpuExitInterrupt(timestamp, cpu)
    }

    /**
     * This checks if the running bit is set
     *
     * @return true if in a softirq. The softirq may be pre-empted by an irq.
     */
    private fun isSoftIrqRaised(state: StateValue?): Boolean =
            (state != null
                    && state is IntegerStateValue
                    && (state.value or StateValues.CPU_STATUS_SOFT_IRQ_RAISED) == StateValues.CPU_STATUS_SOFT_IRQ_RAISED)
}
