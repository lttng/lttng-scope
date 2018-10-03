/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace

import com.efficios.jabberwocky.trace.event.BaseTraceEvent
import com.efficios.jabberwocky.trace.event.TraceEvent
import com.google.common.collect.Iterators

class TraceStubs {

    companion object {
        const val EVENT_NAME_A = "EventA"
        const val EVENT_NAME_B = "EventB"
        const val EVENT_NAME_C = "EventC"
    }

    private class TraceStubIterator(private val trace: TraceStubBase) : TraceIterator<TraceEvent> {

        private var iterator = trace.events.listIterator()

        override fun hasNext() = iterator.hasNext()
        override fun next() = iterator.next()
        override fun hasPrevious() = iterator.hasPrevious()
        override fun previous() = iterator.previous()

        override fun close() {}

        override fun seek(timestamp: Long) {
            /* Just dumbly re-read everything, this is just a test stub... */
            iterator = trace.events.listIterator().apply {
                while (hasNext()) {
                    if (next().timestamp >= timestamp) {
                        previous()
                        break
                    }
                }
            }
        }

        override fun copy(): TraceIterator<TraceEvent> {
            val nbRead = iterator.nextIndex()
            /* Start from the beginning and read the same amount of events that were read. */
            return TraceStubIterator(trace).apply {
                repeat(nbRead, { next() })
            }
        }
    }


    abstract class TraceStubBase() : Trace<TraceEvent>() {

        abstract val events: List<TraceEvent>

        final override fun iterator(): TraceIterator<TraceEvent> {
            return TraceStubIterator(this)
        }
    }

    /** Trace with 3 events, from timestamps [2, 10] */
    class TraceStub1 : TraceStubBase() {
        override val name = "TraceStub1"
        override val events = listOf(
                BaseTraceEvent(this, 2, 0, EVENT_NAME_A, emptyMap(), null),
                BaseTraceEvent(this, 5, 0, EVENT_NAME_B, emptyMap(), null),
                BaseTraceEvent(this, 10, 1, EVENT_NAME_C, emptyMap(), null))
    }

    /** Trace with 3 events, from timestamps [4, 8] */
    class TraceStub2 : TraceStubBase() {
        override val name = "TraceStub2"
        override val events = listOf(
                BaseTraceEvent(this, 4, 1, EVENT_NAME_B, emptyMap(), null),
                BaseTraceEvent(this, 6, 0, EVENT_NAME_B, emptyMap(), null),
                BaseTraceEvent(this, 8, 1, EVENT_NAME_A, emptyMap(), null))

    }

    /** Trace with identical events going from ts 100 to 200, every 2 units */
    class TraceStub3 : TraceStubBase() {
        override val name = "TraceStub3"
        override val events = (100L..200L step 2).map { BaseTraceEvent(this, it, 0, EVENT_NAME_A, emptyMap(), null) }
    }
}
