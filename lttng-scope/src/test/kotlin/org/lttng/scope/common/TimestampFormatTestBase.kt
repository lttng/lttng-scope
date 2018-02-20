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
import org.junit.Before
import org.junit.Test
import org.lttng.scope.application.ScopeOptions
import java.time.ZoneId

abstract class TimestampFormatTestBase(protected val format: TimestampFormat,
                                       private val displayTimeZone: ScopeOptions.DisplayTimeZone,
                                       /** Test strings and their parsed timestamp */
                                       private val stringToTsData: List<Pair<String, Long>>,
                                       /** Strings that should not be valid for this format */
                                       private val invalidStrData: List<String>,
                                       /** Test timestamps and their expected formatted strings */
                                       private val tsToStringData: List<Pair<Long, String>>,
                                       projectRange: TimeRange?) {

    private val projectRange = projectRange ?: TimeRange.of(0, Long.MAX_VALUE)

    @Before
    fun setup() {
        /* Hard-code the "system time zone" to GMT-5 for tests, so that they work everywhere. */
        TimestampFormat.systemTimeZone = ZoneId.of("EST", ZoneId.SHORT_IDS)

        /* Set the application's display option according to the test parameters. */
        ScopeOptions.timestampTimeZone = displayTimeZone
    }

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
