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
import com.efficios.jabberwocky.utils.RewindingIterator

interface TraceIterator<out E : TraceEvent> : RewindingIterator<E>, AutoCloseable {

    /**
     * Bring this iterator to the first event with the given timestamp.
     * Note there might be more than one event at this timestamp!
     */
    fun seek(timestamp: Long)

    /** Return a new iterator at the exact same position as this one. */
    fun copy(): TraceIterator<E>

    /**
     * Close this iterator.
     * Overridden to not throw any exception.
     */
    override fun close()
}
