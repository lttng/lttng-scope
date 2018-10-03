/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

@file:JvmName("TimegraphExample")

package com.efficios.jabberwocky.javeltrace

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.threads.ThreadsModelProvider
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.timegraph.view.json.RenderToJson
import com.google.common.primitives.Longs
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Example of a standalone program generating the JSON for the Threads
 * timegraph model for a given trace and time range.
 */
fun main(args: Array<String>) {

    /* Parse the command-line parameters */
    if (args.size < 4) {
        printUsage()
        return
    }

    val tracePath = args[0]
    val renderStart = Longs.tryParse(args[1])
    val renderEnd = Longs.tryParse(args[2])
    val resolution = Longs.tryParse(args[3])
    if (renderStart == null || renderEnd == null || resolution == null) {
        printUsage()
        return
    }

    /* Create the trace project */
    val projectPath = Files.createTempDirectory("project")
    val trace = CtfTrace(Paths.get(tracePath))
    val project = TraceProject.ofSingleTrace("MyProject", projectPath, trace)

    /* Query for a timegraph render for the requested time range */
    val modelProvider = ThreadsModelProvider()
    val stateModelProvider = modelProvider.stateProvider
    modelProvider.traceProject = project

    val range = TimeRange.of(renderStart, renderEnd)

    val treeRender = modelProvider.getTreeRender()
    val renders = stateModelProvider.getAllStateRenders(treeRender, range, resolution, null)
    RenderToJson.printRenderToStdout(renders)

    /* Cleanup */
    projectPath.toFile().deleteRecursively()
}

private fun printUsage() {
    System.err.println("Cannot parse command-line arguments.")
    System.err.println("Usage: java -jar <jarname>.jar [TRACE PATH] [RENDER START TIME] [RENDER END TIME] [RESOLUTION]")
}