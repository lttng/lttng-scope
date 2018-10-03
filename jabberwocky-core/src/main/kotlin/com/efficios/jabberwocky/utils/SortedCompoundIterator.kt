/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.utils

import com.google.common.collect.Iterators
import com.google.common.collect.PeekingIterator
import java.util.*
import java.util.function.Supplier
import java.util.stream.Collectors

open class SortedCompoundIterator<out T, out I : Iterator<T>>(protected val iterators: Collection<I>,
                                                              comparator: Comparator<T>) : Iterator<T> {

    private val iteratorQueue: Queue<PeekingIterator<T>>

    init {
        if (iterators.isEmpty()) {
            throw IllegalArgumentException()
        }

        val iteratorComparator = Comparator<PeekingIterator<T>> { o1, o2 ->
            /* Empty iterators are placed at the end */
            if (!o1.hasNext() && !o2.hasNext()) 0
            else if (!o1.hasNext()) 1
            else if (!o2.hasNext()) -1
            else compareValuesBy(o1, o2, comparator, { it.peek() })
        }

        iteratorQueue = iterators
                .map { Iterators.peekingIterator(it) }
                .toCollection(PriorityQueue(iterators.size, iteratorComparator))
    }

    override fun hasNext() = iteratorQueue.peek().hasNext()

    override fun next(): T {
        /* Extract the element we will return */
        val elem = iteratorQueue.peek().next()

        /*
         * Remove then re-insert the iterator into the set, so it gets moved to
         * the appropriate position.
         */
        val iter = iteratorQueue.remove()
        iteratorQueue.add(iter)

        return elem
    }

}
