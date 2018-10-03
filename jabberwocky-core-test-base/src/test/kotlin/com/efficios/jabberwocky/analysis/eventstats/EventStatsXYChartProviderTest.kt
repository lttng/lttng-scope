/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.analysis.eventstats

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.tests.JavaFXTestBase
import com.efficios.jabberwocky.trace.TraceStubs
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

private const val PROJECT_NAME = "Test-eventstatsprovider-project"

class EventStatsXYChartProviderTest : JavaFXTestBase() {

    private lateinit var projectPath: Path
    private lateinit var provider: EventStatsXYChartProvider

    @BeforeEach
    fun setup() {
        /* Setup the trace project */
        projectPath = Files.createTempDirectory(PROJECT_NAME)
        val project = TraceProject.ofSingleTrace(PROJECT_NAME, projectPath, TraceStubs.TraceStub3())

        provider = EventStatsXYChartProvider()
        provider.traceProject = project
    }

    @AfterEach
    fun cleanup() {
        provider.traceProject = null
        projectPath.toFile().deleteRecursively()
    }

    /**
     * Try all possible combinations of bounds relative to the project's range.
     * No exception should be thrown!
     */
    @Test
    fun testTimeRangeClamping() {
        val projectStart = provider.traceProject!!.startTime
        val projectEnd = provider.traceProject!!.endTime
        listOf(
                TimeRange.of(projectStart - 10, projectStart - 10),
                TimeRange.of(projectStart - 10, projectStart - 5),
                TimeRange.of(projectStart - 10, projectStart),
                TimeRange.of(projectStart - 10, projectStart + 10),
                TimeRange.of(projectStart - 10, projectEnd),
                TimeRange.of(projectStart - 10, projectEnd + 10),

                TimeRange.of(projectStart, projectStart),
                TimeRange.of(projectStart, projectStart + 10),
                TimeRange.of(projectStart, projectEnd),
                TimeRange.of(projectStart, projectEnd + 10),

                TimeRange.of(projectStart + 10, projectStart + 10),
                TimeRange.of(projectStart + 10, projectEnd),
                TimeRange.of(projectStart + 10, projectEnd + 10),

                TimeRange.of(projectEnd, projectEnd),
                TimeRange.of(projectEnd, projectEnd + 10),

                TimeRange.of(projectEnd + 5, projectEnd + 10),

                TimeRange.of(projectEnd + 10, projectEnd + 10)

        ).forEach {
            provider.generateSeriesRenders(it, 10, null)
        }
    }

}
