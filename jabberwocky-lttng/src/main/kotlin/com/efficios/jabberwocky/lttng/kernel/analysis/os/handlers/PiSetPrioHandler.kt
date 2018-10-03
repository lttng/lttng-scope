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
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngKernelEventLayout
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue
import com.efficios.jabberwocky.trace.event.TraceEvent

class PiSetPrioHandler(layout: LttngKernelEventLayout) : KernelEventHandler(layout) {

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent) {
        val cpu = event.cpu
        val tid = (event.fields[layout.fieldTid] as IntegerValue).value.toInt()
        val prio = (event.fields[layout.fieldNewPrio] as IntegerValue).value.toInt()

        val threadAttributeName = Attributes.buildThreadAttributeName(tid, cpu) ?: return

        val updateThreadNode = ss.getQuarkRelativeAndAdd(ss.getNodeThreads(), threadAttributeName)

        /* Set the current prio for the new process */
        ss.modifyAttribute(event.timestamp,
                StateValue.newValueInt(prio),
                ss.getQuarkRelativeAndAdd(updateThreadNode, Attributes.PRIO))
    }
}
