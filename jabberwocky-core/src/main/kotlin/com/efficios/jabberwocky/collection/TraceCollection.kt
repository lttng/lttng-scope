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
import com.efficios.jabberwocky.trace.event.TraceEvent

class TraceCollection<out E : TraceEvent, out T : Trace<E>>(val traces: Collection<T>) {

    fun iterator(): TraceCollectionIterator<E> {
        return BaseTraceCollectionIterator(this)
    }

}