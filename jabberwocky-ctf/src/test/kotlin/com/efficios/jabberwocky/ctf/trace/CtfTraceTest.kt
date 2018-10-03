/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.lttng.scope.ttt.ctf.CtfTestTrace

class CtfTraceTest {

    companion object {
        private lateinit var ETT1: ExtractedCtfTestTrace
        private lateinit var ETT2: ExtractedCtfTestTrace

        @BeforeAll
        @JvmStatic
        fun setupClass() {
            ETT1 = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)
            ETT2 = ExtractedCtfTestTrace(CtfTestTrace.TRACE2)
        }

        @AfterAll
        @JvmStatic
        fun teardownClass() {
            ETT1.close()
            ETT2.close()
        }
    }

    @Test
    fun testName() {
        assertEquals("kernel", ETT1.trace.name)
        assertEquals("trace2", ETT2.trace.name)
    }

}