/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.ust.trace

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.lttng.testutils.ExtractedCtfTestTrace
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.lttng.scope.ttt.ctf.CtfTestTrace

class LttngUstTraceUtilsTest {

    companion object {
        private lateinit var KERNEL_TRACE: ExtractedCtfTestTrace
        private lateinit var UST_TRACE: ExtractedCtfTestTrace

        @BeforeAll
        @JvmStatic
        fun setupClass() {
            KERNEL_TRACE = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)
            UST_TRACE = ExtractedCtfTestTrace(CtfTestTrace.CYG_PROFILE)
        }

        @AfterAll
        @JvmStatic
        fun teardownClass() {
            KERNEL_TRACE.close()
            UST_TRACE.close()
        }
    }

    @Test
    fun testOpeningKernelTrace() {
        val path = UST_TRACE.trace.tracePath
        val trace = CtfTrace(path)
        assertTrue(trace.isUstTrace())
    }

    @Test
    fun testOpeningNonKernelTrace() {
        val path = KERNEL_TRACE.trace.tracePath
        val trace = CtfTrace(path)
        assertFalse(trace.isUstTrace())
    }
}