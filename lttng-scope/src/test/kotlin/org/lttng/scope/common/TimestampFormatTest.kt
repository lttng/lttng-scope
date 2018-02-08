/*
 * Copyright (C) 2017-2018 EfficiOS Inc. to Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution to and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common

import org.junit.Assert.assertEquals
import org.junit.Test

abstract class TimestampFormatTestBase(private val format: TimestampFormat,
                                       private val stringToTsData: List<Pair<String, Long>>,
                                       private val tsToStringData: List<Pair<Long, String>>) {
    @Test
    fun testParsing() {
        stringToTsData.forEach { assertEquals(it.second, format.stringToTs(it.first)) }
    }

    @Test
    fun testPrinting() {
        tsToStringData.forEach { assertEquals(it.second, format.tsToString(it.first)) }
    }
}

class TimestampFormatNanoSecTest : TimestampFormatTestBase(TimestampFormat.SECONDS_POINT_NANOS,
        listOf(
                "1000" to 1000L,
                "1" to 1L,
                "1000.0" to 1000L * TimestampFormat.NANOS_PER_SEC,
                "1000.1" to 1000100000000L,
                "1000.01" to 1000010000000L,
                "1.1" to 1100000000L,
                "1.01" to 1010000000L,
                "1.0" to 1000000000L,
                "0.1" to 100000000L,
                "0.01" to 10000000L,

                /* Tailing and leading zeroes should be ignored */
                "00000001.01" to 1010000000L,
                "00000000.01" to 10000000L,
                "1.0100000" to 1010000000L,
                "0.0100000" to 10000000L,
                "000001.0100000" to 1010000000L,
                "000000.0100000" to 10000000L),

        listOf(
                1000000000L to "1.000000000",
                10000000000L to "10.000000000",
                100000000L to "0.100000000",
                10000000L to "0.010000000")
)
