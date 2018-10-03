/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

@file:JvmName("Javeltrace")

package com.efficios.jabberwocky.javeltrace

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.trace.event.FieldValue
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * Simple standalone program taking a trace path in parameter and printing all
 * its events to stdout, one per line.
 *
 * Like Babeltrace, but in Java, hence the name.
 *
 * @author Alexandre Montplaisir
 */
fun main(args: Array<String>) {

    /* Parse the command-line parameters. */
    val tracePath = args[0]

    /* We're not keeping any state, so use a temporary directory. */
    val projectPath = Files.createTempDirectory("javeltrace-working-dir")

    /* Create the trace project */
    val trace = CtfTrace(Paths.get(tracePath))
    val project = TraceProject.ofSingleTrace("MyProject", projectPath, trace)

    /* Retrieve an iterator on the project and read its events. */
    project.iterator().use {
        var prevTimestamp: Long = 0
        var i = 0

        while (it.hasNext()) {
            val event = it.next()
            val offset = event.timestamp - prevTimestamp
            printEvent(event, offset)
            prevTimestamp = event.timestamp
            i++
        }
    }

    /* Cleanup */
    projectPath.toFile().deleteRecursively()
}

private fun printEvent(event: CtfTraceEvent, offset: Long) {
    val ts = event.timestamp
    // TODO Correct the timestamp
//    val ts2 = trace.innerTrace.timestampCyclesToNanos(ts)
//    val timestampStr = TS_FORMAT.format(ts2)
//    val offsetStr = TS_FORMAT.format(offset)
    val timestampStr = ts.toString()
    val offsetStr = String.format("%09d", offset)

    val sj = StringJoiner(" ")
            .add("[$timestampStr]")
            .add("(+$offsetStr)")
            .add(event.eventName + ":")
            .add("{ cpu_id = " + event.cpu.toString() + " },")

    val fieldJoiner = StringJoiner(", ", "{ ", " }")
    for (field in event.fields.entries) {
        fieldJoiner.add(field.key + " = " + field.value.toString())
    }
    sj.add(fieldJoiner.toString())

    System.out.println(sj.toString())
}
