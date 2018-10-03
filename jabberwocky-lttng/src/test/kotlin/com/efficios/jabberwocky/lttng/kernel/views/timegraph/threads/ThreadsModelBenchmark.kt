/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

@file:JvmName("ThreadsModelBenchmark")

package com.efficios.jabberwocky.lttng.kernel.views.timegraph.threads

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.timegraph.model.provider.states.TimeGraphModelStateProvider
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateRender
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender
import java.nio.file.Files
import java.nio.file.Paths

private const val PROJECT_NAME = "benchmark-project"
private const val RUNS = 10
private const val TARGET_NB_PIXELS = 2000

/**
 * Benchmark of the {@link ThreadsModelProvider} and
 * {@link ThreadsModelStateProvider} for a target trace
 * passed as parameter.
 */
fun main(args: Array<String>) {

    if (args.size < 2) {
        printUsage()
        return
    }

    val tracePath = args[0]
    val requestedTreeElemNb = args[1].toIntOrNull()
    if (requestedTreeElemNb == null) {
        printUsage()
        return
    }

    /** Function to query the given time range */
    fun query(stateProvider: TimeGraphModelStateProvider,
              treeRender: TimeGraphTreeRender,
              timeRange: TimeRange,
              resolution: Long): List<TimeGraphStateRender> {

        val ret = if (requestedTreeElemNb <= 0) {
            stateProvider.getAllStateRenders(treeRender, timeRange, resolution, null)
        } else {
            val treeElems = treeRender.allTreeElements.subList(0, requestedTreeElemNb).toSet()
            val renders = stateProvider.getStateRenders(treeElems, timeRange, resolution, null)
            renders.values.toList()
        }

        ret.forEach { println("Computed tree element: " + it.treeElement) }
        return ret
    }

    /* Setup the trace and project */
    val projectPath = Files.createTempDirectory(PROJECT_NAME)
    val trace = CtfTrace(Paths.get(tracePath))
    println("Creating project from trace $tracePath")
    val traceProject = TraceProject.ofSingleTrace(PROJECT_NAME, projectPath, trace)

    /* Compute the time ranges that will be used for queries */
    val traceStart = traceProject.startTime
    val traceEnd = traceProject.endTime
    val time_1_8 = ((traceEnd - traceStart) / 8) + traceStart
    val time_2_8 = (2 * (traceEnd - traceStart) / 8) + traceStart
    val time_6_8 = (6 * (traceEnd - traceStart) / 8) + traceStart
    val time_7_8 = (7 * (traceEnd - traceStart) / 8) + traceStart
    val tr1 = TimeRange.of(time_1_8, time_2_8)
    val tr2 = TimeRange.of(time_6_8, time_7_8)
    val resolution = tr1.duration / TARGET_NB_PIXELS
    println("resolution=$resolution")

    /* Setup the model provider */
    val modelProvider = ThreadsModelProvider()
    modelProvider.traceProject = traceProject

    val treeRender = modelProvider.getTreeRender()
    if (treeRender == TimeGraphTreeRender.EMPTY_RENDER) {
        printErr("Analysis produced an empty tree model. Exiting.")
        return
    }
    val actualTreeElemNb = treeRender.allTreeElements.size
    if (requestedTreeElemNb >= actualTreeElemNb) {
        printErr("Analysis produced ")
    }
    val stateProvider = modelProvider.stateProvider

    /* Do a first set of queries to prime the caches. */
    println("Priming queries...")
    val stateRenders1 = query(stateProvider, treeRender, tr1, resolution)
    val stateRenders2 = query(stateProvider, treeRender, tr2, resolution)

    /* Compute the number of intervals in each state render, we'll print that at the end. */
    val count1 = stateRenders1.flatMap { it.stateIntervals }.count()
    val count2 = stateRenders2.flatMap { it.stateIntervals }.count()

    println("Querying")
    val results = (1..RUNS)
            .map {
                println("Run #$it of $RUNS")

                val start1 = System.nanoTime()
                query(stateProvider, treeRender, tr1, resolution)
                val end1 = System.nanoTime()

                /* Second query elsewhere to wipe the cache locality */
                val start2 = System.nanoTime()
                query(stateProvider, treeRender, tr2, resolution)
                val end2 = System.nanoTime()
                Pair(end1 - start1, end2 - start2)
            }

    println()
    val avg1 = results.map { it.first }.average()
    val avg2 = results.map { it.second }.average()
    println("State model for trace $tracePath for range $tr1, averaged over $RUNS runs.")
    println("Interval count in render: $count1")
    println("Average: $avg1 ns")
    println("State model for trace $tracePath for range $tr2, averaged over $RUNS runs.")
    println("Interval count in render: $count2")
    println("Average: $avg2 ns")

    /* Cleanup */
    projectPath.toFile().deleteRecursively()
}

private fun printUsage() {
    printErr("Needs the following program arguments:")
    printErr("1 - [Path to trace]")
    printErr("2 - [Number of tree elements to render] (0 for all)")
}

private fun printErr(errorMsg: String) {
    System.err.println(errorMsg)
}
