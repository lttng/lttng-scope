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

class SoftIrqRaiseHandler(layout: LttngKernelEventLayout) : KernelEventHandler(layout) {

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent) {
        val cpu = event.cpu

        val softIrqId = (event.fields[layout.fieldVec] as IntegerValue).value.toInt()

        /* Mark this SoftIRQ as *raised* in the resource tree. */
        val quark = ss.getQuarkRelativeAndAdd(ss.getNodeSoftIRQs(cpu), softIrqId.toString())
        val sv = if (isInSoftirq(ss.queryOngoingState(quark))) {
            StateValues.SOFT_IRQ_RAISED_RUNNING_VALUE
        } else {
            StateValues.SOFT_IRQ_RAISED_VALUE
        }
        ss.modifyAttribute(event.timestamp, sv, quark)
    }

    private fun isInSoftirq(state: StateValue?): Boolean {
        return (state != null
                && state is IntegerStateValue
                && (state.value or StateValues.CPU_STATUS_SOFTIRQ) == StateValues.CPU_STATUS_SOFTIRQ)
    }
}
