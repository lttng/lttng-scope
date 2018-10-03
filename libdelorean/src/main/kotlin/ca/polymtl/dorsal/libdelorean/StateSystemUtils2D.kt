/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean

import ca.polymtl.dorsal.libdelorean.interval.StateInterval
import com.google.common.annotations.VisibleForTesting
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Query the state system in a two-dimensional fashion in one go, one
 * dimension being time and the other being a set of quarks.
 *
 * @return An iterator returning the matching state intervals. Each step of
 *         the iteration will contain a set of intervals that are all at the
 *         same timestamp (usually aligned on the "resolution points").
 */
fun IStateSystemReader.iterator2D(rangeStart: Long,
                                  rangeEnd: Long,
                                  resolution: Long,
                                  quarks: Set<Int>): Iterator<IterationStep2D> {

    if (rangeStart > rangeEnd) throw IllegalArgumentException("Invalid time range [$rangeStart, $rangeEnd], start is higher than end.")

    if (rangeEnd <= this.startTime || rangeStart >= this.currentEndTime) {
        /* The range is completely outside of the state system's range, nothing will be found. */
        return Collections.emptyIterator()
    }

    if (quarks.isEmpty()) {
        return Collections.emptyIterator()
    }

    val actualStart = max(rangeStart, startTime)
    val actualEnd = min(rangeEnd, currentEndTime)

    return StateIterator2D(this, actualStart, actualEnd, resolution, quarks)
}

class IterationStep2D(val ts: Long, val queryResults: Map<Int, StateInterval>)

internal class StateIterator2D(private val ss: IStateSystemReader,
                               private val rangeStart: Long,
                               private val rangeEnd: Long,
                               private val resolution: Long,
                               quarks: Set<Int>) : AbstractIterator<IterationStep2D>() {

    private val prio: Queue<QueryTarget> = PriorityQueue(quarks.size, compareBy { it.ts })

    init {
        quarks.forEach { prio.offer(QueryTarget(it, rangeStart)) }
    }

    override fun computeNext() {
        val firstElement = prio.poll()
        val queryTs = firstElement.ts
        if (queryTs > rangeEnd) {
            return done()
        }

        /*
         * One iteration step will contain all the intervals at the same
         * timestamp. Pull from the queue all the values for the same
         * timestamp.
         */
        val targets = mutableSetOf<QueryTarget>()
        targets.add(firstElement)
        while (prio.peek()?.ts == queryTs) {
            targets.add(prio.poll())
        }

        /* Do the partial state system query for the retrieved targets */
        val queryQuarks = targets.map { it.quark }.toSet()
        val queryResults = ss.queryStates(queryTs, queryQuarks)

        /* Compute the next query targets and re-insert in the queue */
        queryResults.forEach {
            val nextTs = determineNextQueryTs(it.value, rangeStart, queryTs, resolution)
            prio.offer(QueryTarget(it.key, nextTs))
        }

        /*
         * Only return the intervals that cross the current resolution point
         * *and* the next one.
         */
        val nextResPoint = if (queryTs == rangeEnd) {
            queryTs + resolution
        } else {
            min(queryTs + resolution, rangeEnd)
        }
        val results = queryResults.filter { it.value.intersects(nextResPoint) }
        return setNext(IterationStep2D(queryTs, results))
    }

    private inner class QueryTarget(val quark: Int, val ts: Long) {
        init {
            /* "ts" should always be a multiple of the resolution */
            if ((ts - rangeStart) % resolution != 0L) {
                throw IllegalArgumentException()
            }
        }
    }
}

@VisibleForTesting
internal fun determineNextQueryTs(interval: StateInterval,
                                  rangeStart: Long,
                                  currentQueryTs: Long,
                                  resolution: Long): Long {

    if (!interval.intersects(currentQueryTs)) {
        /* Logic error! */
        throw IllegalStateException()
    }

    val nextResolutionPoint = currentQueryTs + resolution
    return if (nextResolutionPoint > interval.end) {
        nextResolutionPoint
    } else {
        val base = interval.end - rangeStart + 1
        val newBase = roundToClosestHigherMultiple(base, resolution)
        newBase + rangeStart
    }
}

/**
 * Find the multiple of 'multipleOf' that is greater but closest to
 * 'number'. If 'number' is already a multiple of 'multipleOf', the same
 * value will be returned.
 *
 * @param number
 *            The starting number
 * @param multipleOf
 *            We want the returned value to be a multiple of this number
 * @return The closest, greater multiple
 */
private fun roundToClosestHigherMultiple(number: Long, multipleOf: Long): Long {
    return (ceil(number.toDouble() / multipleOf) * multipleOf).toLong()
}
