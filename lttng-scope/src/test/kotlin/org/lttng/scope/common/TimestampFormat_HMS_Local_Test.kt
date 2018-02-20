/*
 * Copyright (C) 2018 EfficiOS Inc. to Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution to and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common

import com.efficios.jabberwocky.common.TimeRange
import org.junit.Test
import org.lttng.scope.application.ScopeOptions

class TimestampFormat_HMS_Local_Test : TimestampFormatTestBase(TimestampFormat.HMS_N,
        ScopeOptions.DisplayTimeZone.LOCAL,
        listOf(
                "14:50:47.314038062" to 1331668247_314038062L,

                "14:50:48.314000000" to 1331668248_314000000L,
                "14:50:48.314" to 1331668248_314000000L,
                "14:50:48.00576" to 1331668248_005760000L,
                "14:50:48.00576000" to 1331668248_005760000L,

                "14:50:48.000000000" to 1331668248_000000000L,
                "14:50:48.0000" to 1331668248_000000000L,
                "14:50:48.0" to 1331668248_000000000L,
                "14:50:48." to 1331668248_000000000L,
                "14:50:48" to 1331668248_000000000L
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
                1331668247_314038062L to "14:50:47.314038062",
                1331668247_314038000L to "14:50:47.314038000",
                1331668247_300000062L to "14:50:47.300000062",
                1331668247_000000062L to "14:50:47.000000062",
                1331668247_000000000L to "14:50:47.000000000"
        ),
        TimeRange.of(1331668247_314038062, 1331668259_054285979)
) {
    /**
     * Fail if the project range is > 24 hours
     */
    @Test(expected = IllegalArgumentException::class)
    fun testInvalidRange() {
        val range = TimeRange.of(1331668247_314038062, 1332000000_000000000)
        format.stringToTs(range, "14:50:47.314038062")
    }
}
