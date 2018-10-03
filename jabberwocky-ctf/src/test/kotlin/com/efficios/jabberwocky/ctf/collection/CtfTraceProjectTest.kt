/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.collection

import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.trace.event.FieldValue
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.lttng.scope.ttt.ctf.CtfTestTrace
import java.nio.file.Files
import java.nio.file.Path

class CtfTraceProjectTest {

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

        private val projectName = "Test-project"
    }

    private lateinit var projectPath: Path
    private lateinit var fixture: TraceProject<CtfTraceEvent, CtfTrace>

    @BeforeEach
    fun setup() {
        projectPath = Files.createTempDirectory(projectName)

        /* Put the first two traces in one collection, and the third one by itself */
        val collection1 = TraceCollection(listOf(ETT1.trace, ETT2.trace))
        val collection2 = TraceCollection(listOf(ETT3.trace))
        fixture = TraceProject(projectName, projectPath, listOf(collection1, collection2))
    }

    @AfterEach
    fun cleanup() {
        projectPath.toFile().deleteRecursively()
    }

    @Test
    fun testEventCount() {
        val expectedCount = CtfTestTrace.KERNEL.nbEvents + CtfTestTrace.TRACE2.nbEvents + CtfTestTrace.KERNEL_VM.nbEvents
        var actualCount = 0
        fixture.iterator().use {
            actualCount = it.asSequence().count()
        }
        assertEquals(expectedCount, actualCount)
    }

    @Test
    fun testSeeking() {
        val targetTimestamp = 1331668247_414253139L
        val targetEvent = CtfTraceEvent(ETT2.trace, targetTimestamp, 0, "exit_syscall",
                mapOf("ret" to FieldValue.IntegerValue(2))
        )
        val nextEvent = CtfTraceEvent(ETT2.trace, 1331668247_414253820, 0, "sys_read",
                mapOf("fd" to FieldValue.IntegerValue(10),
                        "buf" to FieldValue.IntegerValue(0x7FFF6D638FA2, 16),
                        "count" to FieldValue.IntegerValue(8189))
        )
        val prevEvent = CtfTraceEvent(ETT2.trace, 1331668247_414250616, 0, "sys_read",
                mapOf("fd" to FieldValue.IntegerValue(10),
                        "buf" to FieldValue.IntegerValue(0x7FFF6D638FA0, 16),
                        "count" to FieldValue.IntegerValue(8191))
        )

        fixture.iterator().use {
            it.seek(targetTimestamp)
            assertEquals(targetEvent, it.next())
            assertEquals(nextEvent, it.next())

            assertEquals(nextEvent, it.previous())
            assertEquals(targetEvent, it.previous())
            assertEquals(prevEvent, it.previous())
        }
    }

    // TODO More tests, especially with overlapping traces
}
