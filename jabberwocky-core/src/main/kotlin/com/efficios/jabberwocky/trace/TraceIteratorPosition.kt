/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace

import com.efficios.jabberwocky.trace.event.TraceEvent

class TraceIteratorPosition<out E : TraceEvent, out I : TraceIterator<E>> private constructor(private val sourceIterator: I) : AutoCloseable {

    companion object {
        /* Generate a position from an iterator's current position */
        fun <E : TraceEvent, I : TraceIterator<E>> of(iterator: TraceIterator<E>) = TraceIteratorPosition(iterator.copy())
    }

    /** Instantiate a new iterator at this position */
    fun newIterator() = sourceIterator.copy()

    /** Since the position is backed by its own iterator, it needs to be close()'d too, unfortunately. */
    override fun close() = sourceIterator.close()
}
