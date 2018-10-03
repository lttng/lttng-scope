/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace.event

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.trace.event.FieldValue
import com.google.common.collect.ImmutableMap
import org.eclipse.tracecompass.ctf.core.CTFStrings
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition

class CtfTraceEventFactory(private val trace: CtfTrace) {

    companion object {
        private const val UNDERSCORE = "_"
        private const val CONTEXT_FIELD_PREFIX = "context."
    }

    fun createEvent(eventDef: IEventDefinition): CtfTraceEvent {
        /* lib quirk, eventDef.getTimestamp() actually returns a cycle count... */
        val cycles = eventDef.getTimestamp()
        val ts = trace.innerTrace.timestampCyclesToNanos(cycles)

        val cpu = eventDef.getCPU()
        val eventName = eventDef.getDeclaration().getName()

        /* Handle the special case of lost events */
        if (eventName.equals(CTFStrings.LOST_EVENT_NAME)) {
            val nbLostEventsDef = eventDef.getFields().getDefinition(CTFStrings.LOST_EVENTS_FIELD)
            val durationDef = eventDef.getFields().getDefinition(CTFStrings.LOST_EVENTS_DURATION)
            val nbLostEvents = (nbLostEventsDef as IntegerDefinition).getValue()
            val duration = (durationDef as IntegerDefinition).getValue()
            val endTime = ts + duration

            return CtfTraceLostEvent(trace, ts, endTime, cpu, eventName, nbLostEvents)
        }

        // TODO Rest could be lazy-loaded at some point?

        val fields: ImmutableMap.Builder<String, FieldValue> = ImmutableMap.builder()

        /* Parse the event fields (payload) */
        val fieldsDef = eventDef.fields
        if (fieldsDef != null && fieldsDef.fieldNames != null) {
            for (fieldName in fieldsDef.fieldNames) {
                /* Strip the underscore from the field name if there is one */
                val usedFieldName = if (fieldName.startsWith(UNDERSCORE)) fieldName.substring(1) else fieldName
                fields.put(usedFieldName, CtfTraceEventFieldParser.parseField(fieldsDef.getDefinition(fieldName)))
            }
        }

        /* Add context information */
        val contextDef = eventDef.getContext()
        if (contextDef != null) {
            for (contextName in contextDef.fieldNames) {
                /*
                 * Prefix the name we'll use with "context.", and remove
                 * leading underscores if needed.
                 */
                val usedContextName = if (contextName.startsWith(UNDERSCORE)) {
                    CONTEXT_FIELD_PREFIX + contextName.substring(1)
                } else {
                    CONTEXT_FIELD_PREFIX + contextName
                }
                fields.put(usedContextName, CtfTraceEventFieldParser.parseField(contextDef.getDefinition(contextName)))
            }
        }

        /* No custom attributes at the moment */
        return CtfTraceEvent(trace, ts, cpu, eventName, fields.build(), null)
    }

}
