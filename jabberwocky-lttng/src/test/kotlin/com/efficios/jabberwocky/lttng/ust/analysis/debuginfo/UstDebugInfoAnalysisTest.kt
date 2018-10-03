/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.ust.analysis.debuginfo

import com.efficios.jabberwocky.lttng.testutils.ExtractedCtfTestTrace
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.tests.JavaFXTestBase
import com.efficios.jabberwocky.trace.Trace
import com.efficios.jabberwocky.trace.event.TraceEvent
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.lttng.scope.ttt.ctf.CtfTestTrace
import java.nio.file.Files

/**
 * Tests for the {@link UstDebugInfoAnalysis}
 *
 * @author Alexandre Montplaisir
 */
class UstDebugInfoAnalysisTest : JavaFXTestBase() {

    companion object {
        private lateinit var REAL_TEST_TRACE: ExtractedCtfTestTrace
        private lateinit var SYNTH_EXEC_TRACE: ExtractedCtfTestTrace
        private lateinit var SYNTH_TWO_PROCESSES_TRACE: ExtractedCtfTestTrace
        private lateinit var SYNTH_BUILDID_DEBUGLINK_TRACE: ExtractedCtfTestTrace
        private lateinit var INVALID_TRACE: ExtractedCtfTestTrace
        private lateinit var NON_UST_TRACE: ExtractedCtfTestTrace

        @BeforeAll
        @JvmStatic
        fun setupClass() {
            REAL_TEST_TRACE = ExtractedCtfTestTrace(CtfTestTrace.DEBUG_INFO4)
            SYNTH_EXEC_TRACE = ExtractedCtfTestTrace(CtfTestTrace.DEBUG_INFO_SYNTH_EXEC)
            SYNTH_TWO_PROCESSES_TRACE = ExtractedCtfTestTrace(CtfTestTrace.DEBUG_INFO_SYNTH_TWO_PROCESSES)
            SYNTH_BUILDID_DEBUGLINK_TRACE = ExtractedCtfTestTrace(CtfTestTrace.DEBUG_INFO_SYNTH_BUILDID_DEBUGLINK)
            INVALID_TRACE = ExtractedCtfTestTrace(CtfTestTrace.CYG_PROFILE)
            NON_UST_TRACE = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)
        }

        @AfterAll
        @JvmStatic
        fun teardownClass() {
            REAL_TEST_TRACE.close()
            SYNTH_EXEC_TRACE.close()
            SYNTH_TWO_PROCESSES_TRACE.close()
            SYNTH_BUILDID_DEBUGLINK_TRACE.close()
            INVALID_TRACE.close()
            NON_UST_TRACE.close()
        }

        private const val PROJECT_NAME = "debug-info-test-project"
    }

    private val analysis = UstDebugInfoAnalysis.instance()


    private fun <E : TraceEvent, T : Trace<E>> createProject(trace: T): TraceProject<E, T> {
        val projectPath = Files.createTempDirectory(PROJECT_NAME)
        return TraceProject.ofSingleTrace(PROJECT_NAME, projectPath, trace)
    }

    private fun disposeProject(project: TraceProject<*, *>) {
        project.directory.toFile().deleteRecursively()
    }

    /**
     * Test that the analysis can execute on a valid trace.
     */
    @Test
    fun testCanExecute() {
        val project = createProject(REAL_TEST_TRACE.trace)
        assertTrue(analysis.appliesTo(project))
        assertTrue(analysis.canExecute(project))
        disposeProject(project)
    }

    /**
     * Test that the analysis correctly refuses to execute on an invalid trace
     * (LTTng-UST < 2.8 in this case).
     */
    @Test
    fun testAppliesButCannotExcecute() {
        val invalidTraceProject = createProject(INVALID_TRACE.trace)
        assertTrue(analysis.appliesTo(invalidTraceProject))
        assertFalse(analysis.canExecute(invalidTraceProject))
        disposeProject(invalidTraceProject)
    }

    @Test
    fun testDoesNotApply() {
        val kernelTraceProject = createProject(NON_UST_TRACE.trace)
        assertFalse(analysis.appliesTo(kernelTraceProject))
        assertFalse(analysis.canExecute(kernelTraceProject))
        disposeProject(kernelTraceProject)
    }

    /**
     * Test that basic execution of the module works well.
     */
    @Test
    fun testExecution() {
        val project = createProject(REAL_TEST_TRACE.trace)
        val ss = analysis.execute(project, null, null)
        assertNotNull(ss)
        assertFalse(ss.getQuarks("*").isEmpty())
        disposeProject(project)
    }

    /**
     * Test that the binary callsite aspect resolves correctly for some
     * user-defined tracepoints in the trace.
     *
     * These should be available even without the binaries with debug symbols
     * being present on the system.
     */
    @Test
    fun testBinaryCallsites() {
        val project = createProject(REAL_TEST_TRACE.trace)
        val ss = analysis.execute(project, null, null)
        val results = UstDebugInfoAnalysisResults(ss)

        /* We want the 32nd(?) event */
        val event = project.iterator().use { it.asSequence().drop(31).first() }

        /* Tests that the aspects are resolved correctly */
        val actual = results.getCallsiteOfEvent(event)?.toString()
        val expected = "/home/simark/src/babeltrace/tests/debug-info-data/libhello_so+0x14d4"
        assertEquals(expected, actual)

        disposeProject(project)
    }

    /**
     * Test the analysis with a test trace doing an "exec" system call.
     */
    @Test
    fun testExec() {
        val vpid: Long = 1337

        val project = createProject(SYNTH_EXEC_TRACE.trace)
        val ss = analysis.execute(project, null, null)
        val results = UstDebugInfoAnalysisResults(ss)

        val expected1 = UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/foo", null, null, false)
        val matchingFile1 = results.getMatchingFile(4000000, vpid, 0x400100)
        assertEquals(expected1, matchingFile1)

        val matchingFile2 = results.getMatchingFile(8000000, vpid, 0x400100)
        assertNull(matchingFile2)

        val expected3 = UstDebugInfoLoadedBinaryFile(0x500000, "/tmp/bar", null, null, false)
        val matchingFile3 = results.getMatchingFile(9000000, vpid, 0x500100)
        assertEquals(expected3, matchingFile3)

        disposeProject(project)
    }

    /**
     * Test the analysis with a test trace with two processes doing a statedump
     * simultaneously.
     */
    @Test
    fun testTwoProcesses() {
        val vpid1: Long = 1337
        val vpid2: Long = 2001

        val project = createProject(SYNTH_TWO_PROCESSES_TRACE.trace)
        val ss = analysis.execute(project, null, null)
        val results = UstDebugInfoAnalysisResults(ss)

        val expected1 = UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/foo",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "/tmp/debuglink1", false)
        val matchingFile1 = results.getMatchingFile(11000000, vpid1, 0x400100)
        assertEquals(expected1, matchingFile1)

        val expected2 = UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/bar",
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "/tmp/debuglink2", false)
        val matchingFile2 = results.getMatchingFile(12000000, vpid2, 0x400100)
        assertEquals(expected2, matchingFile2)

        disposeProject(project)
    }


    /**
     * Test the analysis with a trace with debug_link information.
     */
    @Test
    fun testBuildIDDebugLink() {
        val project = createProject(SYNTH_BUILDID_DEBUGLINK_TRACE.trace)
        val ss = analysis.execute(project, null, null)
        val results = UstDebugInfoAnalysisResults(ss)

        val expected1 = UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/foo_nn", null, null, false)
        val matchingFile1 = results.getMatchingFile(17000000, 1337, 0x400100)
        assertEquals(expected1, matchingFile1)

        val expected2 = UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/foo_yn",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", null, false)
        val matchingFile2 = results.getMatchingFile(18000000, 1338, 0x400100)
        assertEquals(expected2, matchingFile2)

        val expected3 = UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/foo_ny",
                null, "/tmp/debug_link1", false)
        val matchingFile3 = results.getMatchingFile(19000000, 1339, 0x400100)
        assertEquals(expected3, matchingFile3)

        val expected4 = UstDebugInfoLoadedBinaryFile(0x400000, "/tmp/foo_yy",
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "/tmp/debug_link2", false)
        val matchingFile4 = results.getMatchingFile(20000000, 1340, 0x400100)
        assertEquals(expected4, matchingFile4)

        disposeProject(project)
    }

    /**
     * Test the {@link UstDebugInfoAnalysisModule#getAllBinaries} method.
     */
    @Test
    fun testGetAllBinaries() {
        val project = createProject(REAL_TEST_TRACE.trace)
        val ss = analysis.execute(project, null, null)
        val results = UstDebugInfoAnalysisResults(ss)

        val actualBinaries = results.allBinaries.sortedBy { it.filePath }
        val expectedBinaries = listOf(
                UstDebugInfoBinaryFile("/home/simark/src/babeltrace/tests/debug-info-data/libhello_so",
                        "cdd98cdd87f7fe64c13b6daad553987eafd40cbb", null, true),
                UstDebugInfoBinaryFile("/home/simark/src/babeltrace/tests/debug-info-data/test",
                        "0683255d2cf219c33cc0efd6039db09ccc4416d7", null, false),
                UstDebugInfoBinaryFile("[linux-vdso.so.1]", null, null, false),
                UstDebugInfoBinaryFile("/usr/local/lib/liblttng-ust-dl.so.0.0.0",
                        "39c035014cc02008d6884fcb1be4e020cc820366", null, true),
                UstDebugInfoBinaryFile("/usr/lib/libdl-2.23.so",
                        "db3f9be9f4ebe9e2a21e4ae0b4ef7165d40fdfef", null, true),
                UstDebugInfoBinaryFile("/usr/lib/libc-2.23.so",
                        "946025a5cad7b5f2dfbaebc6ebd1fcc004349b48", null, true),
                UstDebugInfoBinaryFile("/usr/local/lib/liblttng-ust.so.0.0.0",
                        "405b0b15daa73eccb88076247ba30356c00d3b92", null, true),
                UstDebugInfoBinaryFile("/usr/local/lib/liblttng-ust-tracepoint.so.0.0.0",
                        "62c028aad38adb5e0910c527d522e8c86a0a3344", null, true),
                UstDebugInfoBinaryFile("/usr/lib/librt-2.23.so",
                        "aba676bda7fb6adb71e100159915504e1a0c17e6", null, true),
                UstDebugInfoBinaryFile("/usr/lib/liburcu-bp.so.4.0.0",
                        "b9dfadea234107f8453bc636fc160047e0c01b7a", null, true),
                UstDebugInfoBinaryFile("/usr/lib/liburcu-cds.so.4.0.0",
                        "420527f6dacc762378d9fa7def54d91c80a6c87e", null, true),
                UstDebugInfoBinaryFile("/usr/lib/libpthread-2.23.so",
                        "d91ed99c8425b7ce5da5bb750662a91038e02a78", null, true),
                UstDebugInfoBinaryFile("/usr/lib/ld-2.23.so",
                        "524eff0527e923e4adc4be9db1ef7475607b92e8", null, true),
                UstDebugInfoBinaryFile("/usr/lib/liburcu-common.so.4.0.0",
                        "f279a6d46a2b846e15e7abd99cfe9fbe8d7f8295", null, true))
                .sortedBy { it.filePath }

        /* Highlights failures more easily */
        for (i in 0 until expectedBinaries.size) {
            assertEquals(expectedBinaries[i], actualBinaries[i])
        }

        assertEquals(actualBinaries, expectedBinaries)

        disposeProject(project)
    }

}
