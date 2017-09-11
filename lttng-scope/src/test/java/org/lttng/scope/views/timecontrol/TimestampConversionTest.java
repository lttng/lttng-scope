/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timecontrol;

import static org.junit.Assert.assertEquals;
import static org.lttng.scope.views.timecontrol.TimestampConversion.stringToTs;
import static org.lttng.scope.views.timecontrol.TimestampConversion.tsToString;

import org.junit.Test;


public class TimestampConversionTest {

    private static long NANOS_PER_SEC = 1000000000L;

    @Test
    public void testParsing() {
        assertEquals(Long.valueOf(1000), stringToTs("1000"));
        assertEquals(Long.valueOf(1), stringToTs("1"));

        assertEquals(Long.valueOf(1000 * NANOS_PER_SEC), stringToTs("1000.0"));
        assertEquals(Long.valueOf(1000100000000L), stringToTs("1000.1"));
        assertEquals(Long.valueOf(1000010000000L), stringToTs("1000.01"));
        assertEquals(Long.valueOf(1100000000L), stringToTs("1.1"));
        assertEquals(Long.valueOf(1010000000L), stringToTs("1.01"));
        assertEquals(Long.valueOf(1000000000L), stringToTs("1.0"));
        assertEquals(Long.valueOf(100000000L), stringToTs("0.1"));
        assertEquals(Long.valueOf(10000000L), stringToTs("0.01"));

        /* Tailing and leading zeroes should be ignored */
        assertEquals(Long.valueOf(1010000000L), stringToTs("00000001.01"));
        assertEquals(Long.valueOf(10000000L), stringToTs("00000000.01"));
        assertEquals(Long.valueOf(1010000000L), stringToTs("1.0100000"));
        assertEquals(Long.valueOf(10000000L), stringToTs("0.0100000"));
        assertEquals(Long.valueOf(1010000000L), stringToTs("000001.0100000"));
        assertEquals(Long.valueOf(10000000L), stringToTs("000000.0100000"));
    }

    @Test
    public void testPrinting() {
        assertEquals("1.000000000" ,tsToString(1000000000L));
        assertEquals("10.000000000" ,tsToString(10000000000L));
        assertEquals("0.100000000" ,tsToString(100000000L));
        assertEquals("0.010000000" ,tsToString(10000000L));
    }
}
