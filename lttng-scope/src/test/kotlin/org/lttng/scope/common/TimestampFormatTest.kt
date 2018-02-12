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

abstract class TimestampFormatTestBase(protected val format: TimestampFormat,
                                       /** Test strings and their parsed timestamp */
                                       private val stringToTsData: List<Pair<String, Long>>,
                                       /** Strings that should not be valid for this format */
                                       private val invalidStrData: List<String>,
                                       /** Test timestamps and their expected formatted strings */
                                       private val tsToStringData: List<Pair<Long, String>>,
                                       projectRange: TimeRange?) {

    private val projectRange = projectRange ?: TimeRange.of(0, Long.MAX_VALUE)

    @Test
    fun testParsing() {
        stringToTsData.forEach { assertEquals(it.first, it.second, format.stringToTs(projectRange, it.first)) }
    }

    @Test
    fun testParsingInvalid() {
        invalidStrData.forEach { assertNull(it, format.stringToTs(projectRange, it)) }
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
                "2012-03-13 19:50:47" to 1331668247000000000L
        ),
        listOf(
                "abcdef",
                "1afe3",
                "1000",
                "1000.1",
                "19:50:47" /* missing date */
        ),
        listOf(
                1331668247314038062L to "2012-03-13 19:50:47.314038062",
                1331668247000000000L to "2012-03-13 19:50:47.000000000"
        ),
        null
)

class TimestampFormatHMSTest : TimestampFormatTestBase(TimestampFormat.HMS_N,
        listOf(
                "19:50:47.314038062" to 1331668247_314038062L,

                "19:50:48.314000000" to 1331668248_314000000L,
                "19:50:48.314" to 1331668248_314000000L,
                "19:50:48.00576" to 1331668248_005760000L,
                "19:50:48.00576000" to 1331668248_005760000L,

                "19:50:48.000000000" to 1331668248_000000000L,
                "19:50:48.0000" to 1331668248_000000000L,
                "19:50:48.0" to 1331668248_000000000L,
                "19:50:48." to 1331668248_000000000L,
                "19:50:48" to 1331668248_000000000L
        ),
        listOf(
                "abcdef",
                "1afe3",
                "1000",
                "1000.1",
                /* The timestamp would be valid, but date is not allowed in this mode. */
                "2012-03-13 19:50:47",
                /* This format is valid, but the timestamps do not exist in the provided range. */
                "10:05:28.50", /* too early */
                "23:22:21" /* too late */
        ),
        listOf(
                1331668247_314038062L to "19:50:47.314038062",
                1331668247_314038000L to "19:50:47.314038000",
                1331668247_300000062L to "19:50:47.300000062",
                1331668247_000000062L to "19:50:47.000000062",
                1331668247_000000000L to "19:50:47.000000000"
        ),
        TimeRange.of(1331668247_314038062, 1331668259_054285979)
) {
    /**
     * Fail if the project range is > 24 hours
     */
    @Test(expected = IllegalArgumentException::class)
    fun testInvalidRange() {
        val range = TimeRange.of(1331668247_314038062, 1332000000_000000000)
        format.stringToTs(range, "19:50:47.314038062")
    }
}

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
                "000000.0100000" to 10000000L
        ),
        listOf(
                "abcdef",
                "1afe3",
                "22:00:1.123",
                "2018-01-26 11:50:00"
        ),
        listOf(
                1000000000L to "1.000000000",
                10000000000L to "10.000000000",
                100000000L to "0.100000000",
                10000000L to "0.010000000"
        ),
        null
)
