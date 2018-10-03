/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace.event

import com.efficios.jabberwocky.trace.Trace
import com.google.common.base.MoreObjects
import com.google.common.collect.ImmutableMap
import java.text.NumberFormat
import java.util.*

open class BaseTraceEvent(@Transient override val trace: Trace<TraceEvent>,
                          final override val timestamp: Long,
                          final override val cpu: Int,
                          final override val eventName: String,
                          final override val fields: Map<String, FieldValue>,
                          attributes: Map<String, String>? = null) : TraceEvent {

    final override val attributes: Map<String, String> = attributes ?: Collections.emptyMap()

    override fun hashCode() = Objects.hash(timestamp, cpu, eventName, fields, attributes)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as BaseTraceEvent

        if (timestamp != other.timestamp) return false
        if (cpu != other.cpu) return false
        if (eventName != other.eventName) return false
        if (fields != other.fields) return false
        if (attributes != other.attributes) return false

        return true
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
                .add("timestamp", NumberFormat.getInstance().format(timestamp)) //$NON-NLS-1$
                .add("event name", eventName) //$NON-NLS-1$
                .add("cpu", cpu) //$NON-NLS-1$
                .add("fields", fields) //$NON-NLS-1$
                .toString()
    }

}
