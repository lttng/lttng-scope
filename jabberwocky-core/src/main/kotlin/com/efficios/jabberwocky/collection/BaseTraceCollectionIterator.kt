/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.collection

import com.efficios.jabberwocky.trace.Trace
import com.efficios.jabberwocky.trace.TraceIterator
import com.efficios.jabberwocky.trace.event.TraceEvent
import com.efficios.jabberwocky.utils.RewindingSortedCompoundIterator
import com.efficios.jabberwocky.utils.SortedCompoundIterator
import java.util.*

class BaseTraceCollectionIterator<out E : TraceEvent> (traceCollection: TraceCollection<E, Trace<E>>) :
        RewindingSortedCompoundIterator<E, TraceIterator<E>>(traceCollection.traces.map { it.iterator() }, compareBy { event -> event.timestamp }),
        TraceCollectionIterator<E> {

    override fun seek(timestamp: Long) {
        iterators.forEach { it.seek(timestamp) }
        clearCaches()
    }

    override fun close() {
        iterators.forEach { it.close() }
    }

}
