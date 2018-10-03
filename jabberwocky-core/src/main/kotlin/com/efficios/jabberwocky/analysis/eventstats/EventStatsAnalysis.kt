/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.analysis.eventstats

import ca.polymtl.dorsal.libdelorean.IStateSystemWriter
import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis
import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.trace.event.TraceEvent

object EventStatsAnalysis : StateSystemAnalysis() {

    const val TOTAL_ATTRIBUTE = "total"
    const val EVENT_NAME_ATTRIBUTE = "eventname"
    const val CPU_ATTRIBUTE = "cpu"

    override val providerVersion = 0

    override fun appliesTo(project: TraceProject<*, *>) = true

    override fun canExecute(project: TraceProject<*, *>) = true

    override fun filterTraces(project: TraceProject<*, *>): TraceCollection<*, *> {
        /* Applies to all traces in the project */
        return TraceCollection(project.traceCollections.flatMap { it.traces })
    }

    override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent, trackedState: Array<Any>?) {
        /* Increment the totals, then the attributes corresponding to this event's name and cpu */
        val ts = event.timestamp

        val totalQuark = ss.getQuarkAbsoluteAndAdd(TOTAL_ATTRIBUTE)
        ss.incrementAttribute(ts, totalQuark)

        val eventNameQuark = ss.getQuarkAbsoluteAndAdd(EVENT_NAME_ATTRIBUTE, event.eventName)
        ss.incrementAttribute(ts, eventNameQuark)

        val cpuQuark = ss.getQuarkAbsoluteAndAdd(CPU_ATTRIBUTE, event.cpu.toString())
        ss.incrementAttribute(ts, cpuQuark)
    }

}