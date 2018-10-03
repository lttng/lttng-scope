/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.common

import kotlin.math.max
import kotlin.math.min

data class TimeRange private constructor (val startTime: Long, val endTime: Long) {

    init {
        if (endTime < startTime) throw IllegalArgumentException("End of time range earlier than the start:[$startTime, $endTime]")
        if (startTime < 0 || endTime < 0)  throw IllegalArgumentException("One of the bounds is negative:[$startTime, $endTime]")
    }

    companion object {
        @JvmStatic
        fun of(startTime: Long, endTime: Long): TimeRange {
            // TODO Implement caching?
            return TimeRange(startTime, endTime)
        }
    }

    val duration = (endTime - startTime)

    val isSingleTimestamp: Boolean = (startTime == endTime)

    operator fun contains(timestamp: Long): Boolean = (timestamp in startTime..endTime)

}

/**
 * Determine if this interval intersects (has any overlapping range with) another one.
 * The range's bounds are inclusive.
 */
fun TimeRange.intersects(other: TimeRange) = intersects(other.startTime, other.endTime)
fun TimeRange.intersects(lowerLimit: Long, upperLimit: Long) = !(this.endTime < lowerLimit || this.startTime > upperLimit)

/**
 * Compute the intersection between two time ranges, that is the overlapping range common to both.
 * If the ranges don't intersect at all, null is returned.
 */
fun TimeRange.intersection(other: TimeRange): TimeRange? = intersection(other.startTime, other.endTime)
fun TimeRange.intersection(lowerLimit: Long, upperLimit: Long): TimeRange? {
    if (!intersects(lowerLimit, upperLimit)) return null

    val newStart = max(startTime, lowerLimit)
    var newEnd = max(endTime, newStart)
    newEnd = min(newEnd, upperLimit)
    return TimeRange.of(newStart, newEnd)
}
