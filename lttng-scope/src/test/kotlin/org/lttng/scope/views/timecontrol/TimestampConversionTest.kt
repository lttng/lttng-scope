/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timecontrol

import org.junit.Assert.assertEquals
import org.junit.Test
import org.lttng.scope.views.timecontrol.TimestampConversion.NANOS_PER_SEC
import org.lttng.scope.views.timecontrol.TimestampConversion.stringToTs
import org.lttng.scope.views.timecontrol.TimestampConversion.tsToString

class TimestampConversionTest {

    @Test
    fun testParsing() {
        assertEquals(1000L, stringToTs("1000"))
        assertEquals(1L, stringToTs("1"))

        assertEquals(1000L * NANOS_PER_SEC, stringToTs("1000.0"))
        assertEquals(1000100000000L, stringToTs("1000.1"))
        assertEquals(1000010000000L, stringToTs("1000.01"))
        assertEquals(1100000000L, stringToTs("1.1"))
        assertEquals(1010000000L, stringToTs("1.01"))
        assertEquals(1000000000L, stringToTs("1.0"))
        assertEquals(100000000L, stringToTs("0.1"))
        assertEquals(10000000L, stringToTs("0.01"))

        /* Tailing and leading zeroes should be ignored */
        assertEquals(1010000000L, stringToTs("00000001.01"))
        assertEquals(10000000L, stringToTs("00000000.01"))
        assertEquals(1010000000L, stringToTs("1.0100000"))
        assertEquals(10000000L, stringToTs("0.0100000"))
        assertEquals(1010000000L, stringToTs("000001.0100000"))
        assertEquals(10000000L, stringToTs("000000.0100000"))
    }

    @Test
    fun testPrinting() {
        assertEquals("1.000000000", tsToString(1000000000L))
        assertEquals("10.000000000", tsToString(10000000000L))
        assertEquals("0.100000000", tsToString(100000000L))
        assertEquals("0.010000000", tsToString(10000000L))
    }
}
