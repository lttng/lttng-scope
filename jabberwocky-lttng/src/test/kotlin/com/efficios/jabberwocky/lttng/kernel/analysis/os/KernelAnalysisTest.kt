/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package com.efficios.jabberwocky.lttng.kernel.analysis.os

import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.lttng.testutils.ExtractedCtfTestTrace
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.tests.JavaFXTestBase
import com.google.common.io.MoreFiles
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.lttng.scope.ttt.ctf.CtfTestTrace
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Test the {@link KernelAnalysis} class
 *
 * @author Geneviève Bastien
 */
class KernelAnalysisTest : JavaFXTestBase() {

    companion object {
        private lateinit var KERNEL_TRACE: ExtractedCtfTestTrace
        private lateinit var NON_KERNEL_TRACE: ExtractedCtfTestTrace

        @BeforeAll
        @JvmStatic
        fun setupClass() {
            KERNEL_TRACE = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)
            NON_KERNEL_TRACE = ExtractedCtfTestTrace(CtfTestTrace.CYG_PROFILE)
        }

        @AfterAll
        @JvmStatic
        fun teardownClass() {
            KERNEL_TRACE.close()
            NON_KERNEL_TRACE.close()
        }

        private const val PROJECT_NAME = "test-proj"
        private val ANALYSIS = KernelAnalysis
    }

    private var kernelProjectPath: Path? = null
    private var nonKernelProjectPath: Path? = null
    private var hybridProjectPath: Path? = null

    private lateinit var kernelProject: TraceProject<*, *>
    private lateinit var nonKernelProject: TraceProject<*, *>
    private lateinit var hybridProject: TraceProject<*, *>


    /**
     * Set-up the test
     */
    @BeforeEach
    fun setUp() {
        try {
            /* Will produce different paths even if the prefix is the same. */
            kernelProjectPath = Files.createTempDirectory(PROJECT_NAME)
            nonKernelProjectPath = Files.createTempDirectory(PROJECT_NAME)
            hybridProjectPath = Files.createTempDirectory(PROJECT_NAME)
        } catch (e: IOException) {
            fail<Any>(e.message)
        }

        kernelProject = TraceProject.ofSingleTrace(PROJECT_NAME, kernelProjectPath!!, KERNEL_TRACE.trace)
        nonKernelProject = TraceProject.ofSingleTrace(PROJECT_NAME, nonKernelProjectPath!!, NON_KERNEL_TRACE.trace)
        hybridProject = TraceProject(PROJECT_NAME, hybridProjectPath!!,
                listOf(TraceCollection(listOf(KERNEL_TRACE.trace, NON_KERNEL_TRACE.trace))))
    }

    /**
     * Dispose test objects
     */
    @AfterEach
    fun tearDown() {
        listOfNotNull(kernelProjectPath, nonKernelProjectPath, hybridProjectPath)
                .forEach {
                    try {
                        MoreFiles.deleteRecursively(it)
                    } catch (e: IOException) {
                        /* Ignore */
                    }
                }
    }

    @Test
    fun testAppliesTo() {
        assertTrue(ANALYSIS.appliesTo(kernelProject))
        assertFalse(ANALYSIS.appliesTo(nonKernelProject))
        assertTrue(ANALYSIS.appliesTo(hybridProject))
    }

    /**
     * Test the canExecute method on valid and invalid traces
     */
    @Test
    fun testCanExecute() {
        assertTrue(ANALYSIS.canExecute(kernelProject))
        assertFalse(ANALYSIS.canExecute(nonKernelProject))
        assertTrue(ANALYSIS.canExecute(hybridProject))
    }

    /**
     * Test the LTTng kernel analysis execution
     */
    @Test
    fun testAnalysisExecution() {
        listOfNotNull(kernelProject, hybridProject).forEach { project ->
            val ss = ANALYSIS.execute(project, null, null)
            assertNotNull(ss)

            val quarks = ss.getQuarks("*")
            assertFalse(quarks.isEmpty())
        }
    }

}
