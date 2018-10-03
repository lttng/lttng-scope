/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace

import com.efficios.jabberwocky.trace.event.TraceEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class TraceTest {

    companion object {
        @JvmStatic
        private fun getTestTraces() = listOf(
                Arguments.of(TraceStubs.TraceStub1(), 2L, 10L),
                Arguments.of(TraceStubs.TraceStub2(), 4L, 8L)
        )
    }

    @ParameterizedTest
    @MethodSource("getTestTraces")
    fun testStartTime(testTrace: Trace<TraceEvent>,
                      expectedStart: Long,
                      expectedEnd: Long) {
        assertEquals(expectedStart, testTrace.startTime)
    }

    @ParameterizedTest
    @MethodSource("getTestTraces")
    fun testEndTime(testTrace: Trace<TraceEvent>,
                    expectedStart: Long,
                    expectedEnd: Long) {
        assertEquals(expectedEnd, testTrace.endTime)
    }
}