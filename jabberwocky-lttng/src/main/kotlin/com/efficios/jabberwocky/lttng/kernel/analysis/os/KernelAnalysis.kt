/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (c) 2012-2015 Ericsson
 * Copyright (c) 2010-2011 École Polytechnique de Montréal, Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.analysis.os

import ca.polymtl.dorsal.libdelorean.IStateSystemWriter
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException
import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException
import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis
import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.lttng.kernel.trace.getKernelEventLayout
import com.efficios.jabberwocky.lttng.kernel.trace.isKernelTrace
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngKernelEventLayout
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.trace.event.TraceEvent

/**
 * This is the state change input plugin for the state system which handles the kernel traces.
 * <p>
 * Attribute tree:
 * <p>
 * <pre>
 * |- CPUs
 * |  |- <CPU number> -> CPU Status
 * |  |  |- CURRENT_THREAD
 * |  |  |- SOFT_IRQS
 * |  |  |  |- <Soft IRQ number> -> Soft IRQ Status
 * |  |  |- IRQS
 * |  |  |  |- <IRQ number> -> IRQ Status
 * |- THREADS
 * |  |- <Thread number> -> Thread Status
 * |  |  |- PPID
 * |  |  |- EXEC_NAME
 * |  |  |- PRIO
 * |  |  |- SYSTEM_CALL
 * |  |  |- CURRENT_CPU_RQ
 * </pre>
 */
object KernelAnalysis : StateSystemAnalysis() {

    /**
     * Version number of this state provider. Please bump this if you modify the contents of
     * the generated state history in some way.
     */
    private const val VERSION = 28


    // ------------------------------------------------------------------------
    // IAnalysis
    // ------------------------------------------------------------------------

    override fun appliesTo(project: TraceProject<*, *>) = projectContainsKernelTrace(project)
    override fun canExecute(project: TraceProject<*, *>) = projectContainsKernelTrace(project)

    private fun projectContainsKernelTrace(project: TraceProject<*, *>): Boolean =
            project.traceCollections
                    .flatMap { it.traces }
                    // Only applies to CTF traces at the moment
                    .mapNotNull { it as? CtfTrace }
                    .any { it.isKernelTrace() }

    // ------------------------------------------------------------------------
    // StateSystemAnalysis
    // ------------------------------------------------------------------------

    override val providerVersion = VERSION

    override fun filterTraces(project: TraceProject<*, *>): TraceCollection<*, *> =
            project.traceCollections
                    .flatMap { collection -> collection.traces }
                    .mapNotNull { it as? CtfTrace }
                    .filter { it.isKernelTrace() }
                    .let { TraceCollection(it) }

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent, trackedState: Array<Any>?) {
        val trace = event.trace
        if (trace !is CtfTrace || !trace.isKernelTrace()) {
            /* We shouldn't have received this event... */
            return
        }
        // TODO Cache the mapping trace <-> layout so it's not refetched every event.
        // However this needs to be done for one run of the analysis only!
        val defs = KernelAnalysisEventDefinitions.getDefsFromLayout(trace.getKernelEventLayout()!!)
        val eventName = event.eventName

        try {
            /*
             * Feed event to the history system if it's known to cause a state
             * transition.
             */
            val handler = defs.eventNames[eventName]
                    ?: when {
                        isSyscallEntry(defs.layout, eventName) -> defs.sysEntryHandler
                        isSyscallExit(defs.layout, eventName) -> defs.sysExitHandler
                        else -> null
                    }

            handler?.handleEvent(ss, event)

        } catch (ae: AttributeNotFoundException) {
            /*
             * This would indicate a problem with the logic of the manager here,
             * so it shouldn't happen.
             */
            // TODO Re-add logging
//            Activator.instance().logError("Attribute not found: " + ae.getMessage(), ae);

        } catch (tre: TimeRangeException) {
            /*
             * This would happen if the events in the trace aren't ordered
             * chronologically, which should never be the case ...
             */
            // TODO Re-add logging
//            Activator.instance().logError("TimeRangeExcpetion caught in the state system's event manager.\n" +
//                    "Are the events in the trace correctly ordered?\n" + tre.getMessage(), tre);
        }
    }

    private fun isSyscallEntry(layout: LttngKernelEventLayout, eventName: String): Boolean =
            eventName.startsWith(layout.eventSyscallEntryPrefix)
                    || eventName.startsWith(layout.eventCompatSyscallEntryPrefix)

    private fun isSyscallExit(layout: LttngKernelEventLayout, eventName: String): Boolean =
            eventName.startsWith(layout.eventSyscallExitPrefix)
                    || eventName.startsWith(layout.eventCompatSyscallExitPrefix)

}
