/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.collection

import com.efficios.jabberwocky.trace.event.TraceEvent
import com.efficios.jabberwocky.utils.RewindingIterator

interface TraceCollectionIterator<out E : TraceEvent> : RewindingIterator<E>, AutoCloseable {

    /** Seek this iterator to the given timestamp for all traces. */
    fun seek(timestamp: Long)

    /** Overridden to explicitly not throw any exception. */
    override fun close()
}
