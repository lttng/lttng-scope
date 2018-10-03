/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common.tests

import com.efficios.jabberwocky.trace.Trace
import com.efficios.jabberwocky.trace.TraceIterator
import com.efficios.jabberwocky.trace.event.BaseTraceEvent
import com.efficios.jabberwocky.trace.event.TraceEvent
import com.google.common.collect.Iterators
import com.google.common.collect.UnmodifiableIterator

class StubTrace : Trace<TraceEvent>() {

    companion object {
        const val FULL_TRACE_START_TIME = 100_000L
        const val FULL_TRACE_END_TIME = 200_000L
    }

    override val name = "StubTrace"

    private inner class StubTraceIterator : TraceIterator<TraceEvent> {

        private val events: UnmodifiableIterator<TraceEvent> = Iterators.forArray(
                BaseTraceEvent(this@StubTrace, FULL_TRACE_START_TIME, 0, "StubEvent", emptyMap(), null),
                BaseTraceEvent(this@StubTrace, FULL_TRACE_END_TIME, 0, "StubEvent", emptyMap(), null)
        )

        override fun hasNext() = events.hasNext()
        override fun next(): TraceEvent = events.next()
        override fun close() {}
        override fun seek(timestamp: Long) = throw UnsupportedOperationException()
        override fun copy(): TraceIterator<TraceEvent> = throw UnsupportedOperationException()
        override fun hasPrevious(): Boolean = throw UnsupportedOperationException()
        override fun previous(): TraceEvent = throw UnsupportedOperationException()
    }

    override fun iterator(): TraceIterator<TraceEvent> = StubTraceIterator()

}
