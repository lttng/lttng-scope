/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.trace

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngKernel20EventLayout
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngKernel28EventLayout
import com.efficios.jabberwocky.lttng.testutils.ExtractedCtfTestTrace
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.lttng.scope.ttt.ctf.CtfTestTrace

class LttngKernelTraceUtilsTest {

    companion object {
        private lateinit var KERNEL_TRACE: ExtractedCtfTestTrace
        private lateinit var KERNEL_TRACE2: ExtractedCtfTestTrace
        private lateinit var NON_KERNEL_TRACE: ExtractedCtfTestTrace

        @BeforeAll
        @JvmStatic
        fun setupClass() {
            KERNEL_TRACE = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)
            KERNEL_TRACE2 = ExtractedCtfTestTrace(CtfTestTrace.MANY_THREADS)
            NON_KERNEL_TRACE = ExtractedCtfTestTrace(CtfTestTrace.CYG_PROFILE)
        }

        @AfterAll
        @JvmStatic
        fun teardownClass() {
            KERNEL_TRACE.close()
            KERNEL_TRACE2.close()
            NON_KERNEL_TRACE.close()
        }
    }

    @Test
    fun testOpeningKernelTrace() {
        val path = KERNEL_TRACE.trace.tracePath
        val trace = CtfTrace(path)
        assertTrue(trace.isKernelTrace())
    }

    @Test
    fun testOpeningNonKernelTrace() {
        val path = NON_KERNEL_TRACE.trace.tracePath
        val trace = CtfTrace(path)
        assertFalse(trace.isKernelTrace())
    }

    @Test
    fun testEventLayout() {
        assertEquals(LttngKernel20EventLayout.instance, KERNEL_TRACE.trace.getKernelEventLayout())
        assertEquals(LttngKernel28EventLayout.instance, KERNEL_TRACE2.trace.getKernelEventLayout())
    }
}