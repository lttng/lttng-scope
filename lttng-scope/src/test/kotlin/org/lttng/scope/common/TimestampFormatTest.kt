/*
 * Copyright (C) 2017-2018 EfficiOS Inc. to Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution to and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common

import com.efficios.jabberwocky.common.TimeRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

abstract class TimestampFormatTestBase(private val format: TimestampFormat,
                                       /** Test strings and their parsed timestamp */
                                       private val stringToTsData: List<Pair<String, Long>>,
                                       /** Strings that should not be valid for this format */
                                       private val invalidStrData: List<String>,
                                       /** Test timestamps and their expected formatted strings */
                                       private val tsToStringData: List<Pair<Long, String>>) {

    companion object {
        private val fullRange = TimeRange.of(0, Long.MAX_VALUE)
    }

    @Test
    fun testParsing() {
        stringToTsData.forEach { assertEquals(it.first, it.second, format.stringToTs(fullRange, it.first)) }
    }

    @Test
    fun testParsingInvalid() {
        invalidStrData.forEach { assertNull(it, format.stringToTs(fullRange, it)) }
    }

    @Test
    fun testPrinting() {
        tsToStringData.forEach { assertEquals(it.first.toString(), it.second, format.tsToString(it.first)) }
    }
}

class TimestampFormatDHMSTest : TimestampFormatTestBase(TimestampFormat.YMD_HMS_N,
        listOf(
                "2012-03-13 19:50:47.314038062" to 1331668247314038062L,

                /* Trailing zeroes may be omitted */
                "2012-03-13 19:50:47.314000000" to 1331668247314000000L,
                "2012-03-13 19:50:47.314" to 1331668247314000000L,
                "2012-03-13 19:50:47.00576" to 1331668247005760000L,
                "2012-03-13 19:50:47.00576000" to 1331668247005760000L,

                "2012-03-13 19:50:47.000000000" to 1331668247000000000L,
                "2012-03-13 19:50:47.0000" to 1331668247000000000L,
                "2012-03-13 19:50:47.0" to 1331668247000000000L,
                "2012-03-13 19:50:47." to 1331668247000000000L, /* Ending with a decimal point should be valid */
                "2012-03-13 19:50:47" to 1331668247000000000L),

        listOf(
                "abcdef",
                "1afe3",
                "1000",
                "1000.1",
                "19:50:47"), /* missing date */

        listOf(
                1331668247314038062L to "2012-03-13 19:50:47.314038062",
                1331668247000000000L to "2012-03-13 19:50:47.000000000")
)

class TimestampFormatNanoSecTest : TimestampFormatTestBase(TimestampFormat.SECONDS_POINT_NANOS,
        listOf(
                "1000" to 1000L,
                "1" to 1L,
                "1000.0" to 1000L * NANOS_PER_SEC,
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
                "abcdef",
                "1afe3",
                "22:00:1.123",
                "2018-01-26 11:50:00"),

        listOf(
                1000000000L to "1.000000000",
                10000000000L to "10.000000000",
                100000000L to "0.100000000",
                10000000L to "0.010000000")
)
