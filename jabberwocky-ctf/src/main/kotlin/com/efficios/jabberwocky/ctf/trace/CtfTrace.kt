/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEventFactory
import com.efficios.jabberwocky.trace.Trace
import com.efficios.jabberwocky.trace.TraceInitializationException
import org.eclipse.tracecompass.ctf.core.CTFException
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace
import java.nio.file.Path

class CtfTrace(val tracePath: Path) : Trace<CtfTraceEvent>() {

    internal val innerTrace: CTFTrace = try {
        CTFTrace(tracePath.toFile())
    } catch (e: CTFException) {
        throw TraceInitializationException(e)
    }

    override val name = tracePath.fileName.toString()

    override fun iterator(): CtfTraceIterator {
        return CtfTraceIterator(this)
    }

    val eventFactory = CtfTraceEventFactory(this)

    val environment: Map<String, String> = innerTrace.environment
}
