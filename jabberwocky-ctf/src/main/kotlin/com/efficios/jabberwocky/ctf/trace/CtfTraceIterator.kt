/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.trace.TraceIterator
import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.EvictingQueue
import com.google.common.collect.Iterables
import org.eclipse.tracecompass.ctf.core.CTFException
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader
import java.util.*

private const val MAX_CACHE_SIZE = 50_000

open class CtfTraceIterator private constructor(private val originTrace: CtfTrace,
                                                private val forwardIterator: ForwardIterator) : TraceIterator<CtfTraceEvent> {

    constructor(originTrace: CtfTrace) : this(originTrace, ForwardIterator(originTrace))

    @VisibleForTesting
    @Transient
    internal var cacheIterator: ListIterator<CtfTraceEvent>? = null
        private set

    override fun hasNext(): Boolean {
        val cacheIter = cacheIterator
        if (cacheIter == null || !cacheIter.hasNext()) return forwardIterator.hasNext()
        return true
    }

    override fun next(): CtfTraceEvent {
        val cacheIter = cacheIterator
        return if (cacheIter == null) {
            forwardIterator.next()

        } else if (!cacheIter.hasNext()) {
            /* Drop the cache, switch back to the forward iterator. */
            cacheIterator = null
            forwardIterator.next()

        } else {
            /* Read from the cache. */
            cacheIter.next()
        }
    }

    override fun hasPrevious(): Boolean {
        val cacheIter = cacheIterator
        return when {
            cacheIter == null -> {
                val newCacheIterator = newCacheIterator() ?: return false
                cacheIterator = newCacheIterator
                newCacheIterator.hasPrevious()
            }
            cacheIter.hasPrevious() -> /* Continue reading from the cache */
                true
            else -> {
                /* Check if we can load a new cache before this position. */
                val newPivotEvent = cacheIter.next()
                forwardIterator.seek(newPivotEvent.timestamp)

                /* Check in case there are several events at this timestamp */
                while (forwardIterator.peek() != newPivotEvent) {
                    forwardIterator.next()
                }

                val newCacheIterator = newCacheIterator() ?: return false
                cacheIterator = newCacheIterator
                newCacheIterator.hasPrevious()
            }
        }
    }

    override fun previous(): CtfTraceEvent {
        if (!hasPrevious()) throw NoSuchElementException()
        /* Previous call to hasPrevious() should have populated 'cacheIterator' */
        return cacheIterator!!.previous()
    }

    override fun seek(timestamp: Long) {
        cacheIterator = null
        forwardIterator.seek(timestamp)
    }

    override fun copy(): CtfTraceIterator = CtfTraceIterator(originTrace, forwardIterator.copy())

    override fun close() {
        cacheIterator = null
        forwardIterator.close()
    }

    /**
     * Returns null when we are at the beginning already.
     */
    private fun newCacheIterator(): ListIterator<CtfTraceEvent>? {
        var startedFromAfterEnd = false
        /*
         * We will jump back to an earlier timestamp, and start reading from there to populate
         * a cache of events the list-iterator will be able to navigate through.
         *
         * The target timestamp is defined as the highest among all the current packets' start times.
         *
         * // TODO Test/benchmark with min()?
         */
        var currentPackets = forwardIterator.traceReader.currentPacketDescriptors
        if (currentPackets == null || Iterables.isEmpty(currentPackets)) {
            forwardIterator.seek(originTrace.endTime)
            /* Should not be empty/null on next access */
            currentPackets = forwardIterator.traceReader.currentPacketDescriptors
            startedFromAfterEnd = true
        }

        if (!forwardIterator.hasNext()) throw IllegalStateException()

        val limitEvent = forwardIterator.next() /* Should be present */
        val ts = currentPackets
                .map { it.timestampBegin }
                /* Convert cycles -> real timestamp. */
                .map { originTrace.innerTrace.timestampCyclesToNanos(it) }
                .filter { it < limitEvent.timestamp }
                /*
                 * Doesn't seem there is a way to query the "previous packet". Handle all edge cases
                 * (like a last packet containing a single event) by re-reading from the beginning...
                 */
                .max() ?: originTrace.startTime

        forwardIterator.seek(ts)
        if (!forwardIterator.hasNext()) return null

        val list = EvictingQueue.create<CtfTraceEvent>(MAX_CACHE_SIZE)
        while (forwardIterator.hasNext()) {
            val event = forwardIterator.peek()!!
            if (event == limitEvent) break
            list.add(event)
            forwardIterator.next()
        }

        if (startedFromAfterEnd) list.add(limitEvent)

        /* The 'list.size' argument ensures the iterators starts at the last event of the list. */
        return list.toList().listIterator(list.size)
    }

    private class ForwardIterator(private val originTrace: CtfTrace) : Iterator<CtfTraceEvent> {

        val traceReader: CTFTraceReader = try {
            CTFTraceReader(originTrace.innerTrace)
        } catch (e: CTFException) {
            /*
         * If the CtfTrace was initialized successfully, creating an
         * iterator should not fail.
         */
            throw IllegalStateException(e)
        }

        private var currentEventDef: IEventDefinition? = traceReader.currentEventDef

        override fun hasNext(): Boolean = (currentEventDef != null)

        override fun next(): CtfTraceEvent {
            val currentEventDef = currentEventDef ?: throw NoSuchElementException()

            /* Wrap the current event into a JW event */
            val event = originTrace.eventFactory.createEvent(currentEventDef)

            /* Prepare the "next next" event */
            try {
                traceReader.advance()
                this.currentEventDef = traceReader.currentEventDef
            } catch (e: CTFException) {
                /* Shouldn't happen if we did the other checks correctly */
                throw IllegalStateException(e)
            }

            return event
        }

        fun close() {
            traceReader.close()
        }

        fun peek(): CtfTraceEvent? {
            return originTrace.eventFactory.createEvent(currentEventDef ?: return null)
        }

        fun seek(timestamp: Long) {
            // TODO Support/test with multiple events at the same timestamp
            // Current library doesn't give guarantees regarding which events are returned first.

            /* traceReader.seek() works off cycle counts, not timestamps !?! */
            traceReader.seek(originTrace.innerTrace.timestampNanoToCycles(timestamp))
            currentEventDef = traceReader.topStream?.currentEvent
        }

        fun copy(): ForwardIterator {
            val eventDef = currentEventDef
            return ForwardIterator(originTrace).apply {
                /* Here we seek using the *cycle count* directly */
                traceReader.seek(eventDef?.timestamp ?: Long.MAX_VALUE)
                currentEventDef = traceReader.topStream?.currentEvent
            }

        }

    }
}
