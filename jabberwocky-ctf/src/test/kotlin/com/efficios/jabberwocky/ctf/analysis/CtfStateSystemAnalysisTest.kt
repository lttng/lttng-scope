/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.analysis

import ca.polymtl.dorsal.libdelorean.IStateSystemReader
import ca.polymtl.dorsal.libdelorean.IStateSystemWriter
import ca.polymtl.dorsal.libdelorean.statevalue.IntegerStateValue
import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis
import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.tests.JavaFXTestBase
import com.efficios.jabberwocky.trace.event.TraceEvent
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.lttng.scope.ttt.ctf.CtfTestTrace
import java.nio.file.Files
import java.nio.file.Path

class CtfStateSystemAnalysisTest : JavaFXTestBase() {

    companion object {
        private lateinit var ETT1: ExtractedCtfTestTrace
        private lateinit var ETT2: ExtractedCtfTestTrace
        private lateinit var ETT3: ExtractedCtfTestTrace

        @BeforeAll
        @JvmStatic
        fun setupClass() {
            ETT1 = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)
            ETT2 = ExtractedCtfTestTrace(CtfTestTrace.TRACE2)
            ETT3 = ExtractedCtfTestTrace(CtfTestTrace.KERNEL_VM)
        }

        @AfterAll
        @JvmStatic
        fun teardownClass() {
            ETT1.close()
            ETT2.close()
            ETT3.close()
        }

        private val projectName = "Test-statesystem-project"
        private val attribName = "count"
    }


    private lateinit var projectPath: Path
    private lateinit var project: TraceProject<CtfTraceEvent, CtfTrace>

    private lateinit var ss: IStateSystemReader

    @BeforeEach
    fun setup() {
        /* Setup the trace project */
        projectPath = Files.createTempDirectory(projectName)
        /* Put the first two traces in one collection, and the third one by itself */
        val collection1 = TraceCollection(listOf(ETT1.trace, ETT2.trace))
        val collection2 = TraceCollection(listOf(ETT3.trace))
        project = TraceProject(projectName, projectPath, listOf(collection1, collection2))

        /* Execute the analysis */
        ss = TestAnalysis.execute(project)
    }

    @AfterEach
    fun cleanup() {
        ss.dispose()
        projectPath.toFile().deleteRecursively()
    }

    private object TestAnalysis : StateSystemAnalysis() {

        override val providerVersion = 0

        override fun appliesTo(project: TraceProject<*, *>) = true

        override fun canExecute(project: TraceProject<*, *>) = true

        override fun filterTraces(project: TraceProject<*, *>): TraceCollection<*, *> {
            /* Just return all traces in the project */
            return TraceCollection(project.traceCollections.flatMap { it.traces })
        }

        override fun handleEvent(ss: IStateSystemWriter, event: TraceEvent, trackedState: Array<Any>?) {
            /* Count the number of seen events in a "count" attribute */
            val quark = ss.getQuarkAbsoluteAndAdd(attribName)
            ss.incrementAttribute(event.timestamp, quark)
        }

    }

    @Test
    fun testResults() {
        assertEquals(1, ss.nbAttributes)

        /* Check the event count at the end. */
        val expectedEventCount = CtfTestTrace.KERNEL.nbEvents + CtfTestTrace.TRACE2.nbEvents + CtfTestTrace.KERNEL_VM.nbEvents
        val endTime = ss.currentEndTime
        val quark = ss.getQuarkAbsolute(attribName)
        val eventCount = (ss.querySingleState(endTime, quark).stateValue as IntegerStateValue).value
        assertEquals(expectedEventCount, eventCount)

        /* Check the event count at some point in the middle. */
        val halfwayTimestamp = 1347684508_932149675L
        val expectedHWCount = 1290960
        val halfwayCount = (ss.querySingleState(halfwayTimestamp, quark).stateValue as IntegerStateValue).value
        assertEquals(expectedHWCount, halfwayCount)
    }

}