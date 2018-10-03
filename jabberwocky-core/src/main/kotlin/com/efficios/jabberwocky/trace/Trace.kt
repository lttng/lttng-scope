package com.efficios.jabberwocky.trace

import com.efficios.jabberwocky.trace.event.TraceEvent
import com.google.common.collect.Iterators

abstract class Trace<out E : TraceEvent> {

    abstract val name: String

    /* Lazy-load the start time by reading the timestamp of the first event. */
    val startTime: Long by lazy {
        var startTime: Long = 0L
        iterator().use { iter ->
            if (iter.hasNext()) {
                startTime = iter.next().timestamp
            }
        }
        startTime
    }

    val endTime: Long by lazy {
        var endTime: Long = 0L
        iterator().use {
            if (it.hasNext()) {
                endTime = Iterators.getLast(it).timestamp
            }
        }
        endTime
    }

    abstract fun iterator(): TraceIterator<E>
}
