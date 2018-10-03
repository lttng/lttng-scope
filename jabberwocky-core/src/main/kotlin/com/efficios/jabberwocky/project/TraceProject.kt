/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.project

import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.trace.Trace
import com.efficios.jabberwocky.trace.event.TraceEvent
import java.nio.file.Files
import java.nio.file.Path

class TraceProject<out E : TraceEvent, out T : Trace<E>> (val name: String,
                                                          val directory: Path,
                                                          val traceCollections: Collection<TraceCollection<E, T>>) {

    init {
        if (!Files.isReadable(directory) || !Files.isWritable(directory)) throw IllegalArgumentException("Invalid project directory")
        if (traceCollections.isEmpty()) throw IllegalArgumentException("Project needs at least 1 trace")
    }

    companion object {
        @JvmStatic
        fun <X: TraceEvent, Y: Trace<X>> ofSingleTrace(name: String, directory: Path, trace: Y): TraceProject<X, Y> {
            val collection = TraceCollection<X, Y>(setOf(trace))
            return TraceProject<X, Y>(name, directory, setOf(collection))
        }
    }

    fun iterator(): TraceProjectIterator<E> {
        return BaseTraceProjectIterator(this)
    }

    /* The project's start time is the earliest of all its traces's start times */
    val startTime: Long = traceCollections
                .flatMap { collection -> collection.traces }
                .map { trace -> trace.startTime }
                .min() ?: 0L


    /* The project's end time is the latest of all its traces's end times */
    val endTime: Long = traceCollections
            .flatMap { collection -> collection.traces }
            .map { trace -> trace.endTime }
            .max() ?: 0L

    val fullRange = TimeRange.of(startTime, endTime)
}