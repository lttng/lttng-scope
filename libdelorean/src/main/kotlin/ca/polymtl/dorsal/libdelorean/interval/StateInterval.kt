/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.interval

import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue
import com.google.common.base.MoreObjects
import java.util.*

open class StateInterval(val start: Long,
                    val end: Long,
                    val attribute: Int,
                    val stateValue: StateValue) {

    init {
        if (start > end) throw TimeRangeException("Start:$start, End:$end")
    }

    fun intersects(timestamp: Long) = ((start <= timestamp) && (timestamp <= end))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StateInterval

        if (start != other.start) return false
        if (end != other.end) return false
        if (attribute != other.attribute) return false
        if (stateValue != other.stateValue) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(start, end, attribute, stateValue)
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
                .add("start", start)
                .add("end", end)
                .add("attribute", attribute)
                .add("value", stateValue.toString())
                .toString()
    }


}