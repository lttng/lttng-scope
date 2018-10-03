/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.utils

import java.util.*
import kotlin.NoSuchElementException

/**
 * Alternative to [SortedCompoundIterator] which is slightly less efficient,
 * but supports going backwards. It wraps [RewindingIterator] objects instead.
 */
open class RewindingSortedCompoundIterator<out T, out I : RewindingIterator<T>>(protected val iterators: Collection<I>,
                                                                                elementComparator: Comparator<T>) : RewindingIterator<T> {

    private val forwardsComparator = Comparator<ReversePeekingIterator<T>> { o1, o2 ->
        /* Empty iterators are placed at the end */
        when {
            !o1.hasNext() && !o2.hasNext() -> 0
            !o1.hasNext() -> 1
            !o2.hasNext() -> -1
            else -> compareValuesBy(o1, o2, elementComparator, { it.peekNext() })
        }
    }

    private val backwardsComparator = Comparator<ReversePeekingIterator<T>> { o1, o2 ->
        when {
            !o1.hasPrevious() && !o2.hasPrevious() -> 0
            !o1.hasPrevious() -> 1
            !o2.hasPrevious() -> -1
            else -> compareValuesBy(o1, o2, elementComparator.reversed(), { it.peekPrevious() })
        }
    }

    private var peekingIterators = iterators.map { ReversePeekingIterator(it) }

    private var forwardsQueue: Queue<ReversePeekingIterator<T>>? = null
    private var backwardsQueue: Queue<ReversePeekingIterator<T>>? = null

    init {
        if (iterators.isEmpty()) throw IllegalArgumentException()
        loadForwardsQueue()
    }

    override fun hasPrevious(): Boolean {
        if (backwardsQueue == null) loadBackwardsQueue()
        return backwardsQueue!!.peek().hasPrevious()
    }

    override fun previous(): T {
        if (backwardsQueue == null) loadBackwardsQueue()
        val iter = backwardsQueue!!.remove()
        val elem = iter.previous()
        backwardsQueue!!.add(iter)
        return elem
    }

    override fun hasNext(): Boolean {
        if (forwardsQueue == null) loadForwardsQueue()
        return forwardsQueue!!.peek().hasNext()
    }

    override fun next(): T {
        if (forwardsQueue == null) loadForwardsQueue()
        val iter = forwardsQueue!!.remove()
        val elem = iter.next()
        forwardsQueue!!.add(iter)
        return elem
    }

    /**
     * If anything that might invalidate cached next/previous values happens,
     * for example a seek operation, use this to clear them. They will be reloaded
     * by the next calls to next/previous and will make use of the events at
     * the new location.
     */
    fun clearCaches() {
        forwardsQueue = null
        backwardsQueue = null
        peekingIterators = iterators.map { ReversePeekingIterator(it) }
    }

    private fun loadForwardsQueue() {
        backwardsQueue = null
        forwardsQueue = peekingIterators.toCollection(PriorityQueue(peekingIterators.size, forwardsComparator))
    }

    private fun loadBackwardsQueue() {
        forwardsQueue = null
        backwardsQueue = peekingIterators.toCollection(PriorityQueue(peekingIterators.size, backwardsComparator))
    }
}

private class ReversePeekingIterator<out E>(private val rewindingIterator: RewindingIterator<E>) : RewindingIterator<E> {

    private enum class Direction {
        FORWARDS,
        BACKWARDS
    }

    private var direction: Direction
    private var peekedElement: E?

    init {
        direction = Direction.FORWARDS
        peekedElement = if (rewindingIterator.hasNext()) {
            rewindingIterator.next()
        } else {
            null
        }
    }

    private fun goForwards() {
        if (direction == Direction.FORWARDS) return
        direction = Direction.FORWARDS

        peekedElement = if (peekedElement == null) {
            /*
             * We were in backwards mode, at the beginning of the iteration.
             * The next peek should be the first element.
             */
            if (rewindingIterator.hasNext()) rewindingIterator.next() else null
        } else {
            /*
             * We have to advance twice to compensate for the previous peeked element.
             */
            rewindingIterator.next()
            if (rewindingIterator.hasNext()) rewindingIterator.next() else null
        }
    }

    private fun goBackwards() {
        if (direction == Direction.BACKWARDS) return
        direction = Direction.BACKWARDS
        peekedElement = if (peekedElement == null) {
            if (rewindingIterator.hasPrevious()) rewindingIterator.previous() else null
        } else {
            rewindingIterator.previous()
            if (rewindingIterator.hasPrevious()) rewindingIterator.previous() else null
        }
    }

    override fun hasNext(): Boolean {
        goForwards()
        return (peekedElement != null)
    }

    override fun next(): E {
        goForwards()
        val ret = peekedElement ?: throw NoSuchElementException()
        peekedElement = if (rewindingIterator.hasNext()) {
            rewindingIterator.next()
        } else {
            null
        }
        return ret
    }

    override fun hasPrevious(): Boolean {
        goBackwards()
        return (peekedElement != null)
    }

    override fun previous(): E {
        goBackwards()
        val ret = peekedElement ?: throw NoSuchElementException()
        peekedElement = if (rewindingIterator.hasPrevious()) {
            rewindingIterator.previous()
        } else {
            null
        }
        return ret
    }

    fun peekNext(): E {
        goForwards()
        return peekedElement ?: throw NoSuchElementException()
    }

    fun peekPrevious(): E {
        goBackwards()
        return peekedElement ?: throw NoSuchElementException()
    }
}
