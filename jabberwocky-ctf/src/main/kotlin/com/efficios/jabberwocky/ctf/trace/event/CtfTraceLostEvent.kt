/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace.event

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.trace.event.TraceLostEvent
import com.google.common.base.MoreObjects
import java.util.*

class CtfTraceLostEvent(trace: CtfTrace,
                        startTime: Long,
                        endTime: Long,
                        cpu: Int,
                        eventName: String,
                        override val nbLostEvents: Long) : CtfTraceEvent(trace, startTime, cpu, eventName, emptyMap(), null), TraceLostEvent {

    override val timeRange = TimeRange.of(startTime, endTime)

    override fun hashCode(): Int = Objects.hash(super.hashCode(), timeRange, nbLostEvents)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        if (!super.equals(other)) return false

        other as CtfTraceLostEvent

        if (nbLostEvents != other.nbLostEvents) return false
        if (timeRange != other.timeRange) return false

        return true
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
                .add("timerange", timeRange) //$NON-NLS-1$
                .add("event name", eventName) //$NON-NLS-1$
                .add("cpu", cpu) //$NON-NLS-1$
                .add("Nb Lost Events", nbLostEvents)
                .toString()
    }

}
