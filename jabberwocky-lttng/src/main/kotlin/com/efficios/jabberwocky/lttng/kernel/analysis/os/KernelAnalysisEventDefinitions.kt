/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.analysis.os

import com.efficios.jabberwocky.lttng.kernel.analysis.os.handlers.*
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngKernelEventLayout

class KernelAnalysisEventDefinitions private constructor(val layout: LttngKernelEventLayout) {

    companion object {

        private val DEFINITIONS_MAP: MutableMap<LttngKernelEventLayout, KernelAnalysisEventDefinitions> = mutableMapOf()

        @JvmStatic
        @Synchronized
        fun getDefsFromLayout(layout: LttngKernelEventLayout): KernelAnalysisEventDefinitions {
            var definitions = DEFINITIONS_MAP[layout]
            if (definitions == null) {
                definitions = KernelAnalysisEventDefinitions(layout)
                DEFINITIONS_MAP[layout] = definitions
            }
            return definitions
        }
    }

    val sysEntryHandler: KernelEventHandler = SysEntryHandler(layout)
    val sysExitHandler: KernelEventHandler = SysExitHandler(layout)
    val eventNames: Map<String, KernelEventHandler>

    init {
        val map = mutableMapOf(layout.eventIrqHandlerEntry to IrqEntryHandler(layout),
                layout.eventIrqHandlerExit to IrqExitHandler(layout),
                layout.eventSoftIrqEntry to SoftIrqEntryHandler(layout),
                layout.eventSoftIrqExit to SoftIrqExitHandler(layout),
                layout.eventSoftIrqRaise to SoftIrqRaiseHandler(layout),
                layout.eventSchedSwitch to SchedSwitchHandler(layout),
                layout.eventSchedPiSetPrio to PiSetPrioHandler(layout),
                layout.eventSchedProcessFork to ProcessForkHandler(layout),
                layout.eventSchedProcessExit to ProcessExitHandler(layout),
                layout.eventSchedProcessFree to ProcessFreeHandler(layout),
                layout.eventSchedProcessWaking to SchedWakeupHandler(layout),
                layout.eventSchedMigrateTask to SchedMigrateTaskHandler(layout))

        layout.ipiIrqVectorsEntries.forEach { map[it] = IPIEntryHandler(layout) }
        layout.ipiIrqVectorsExits.forEach { map[it] = IPIExitHandler(layout) }

        layout.eventStatedumpProcessState?.let { map[it] = StateDumpHandler(layout) }

        layout.eventsSchedWakeup.forEach { map[it] = SchedWakeupHandler(layout) }

        eventNames = map
    }
}
