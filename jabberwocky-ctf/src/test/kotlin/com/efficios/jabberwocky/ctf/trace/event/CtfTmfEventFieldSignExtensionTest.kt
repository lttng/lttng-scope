/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace.event

import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace
import com.efficios.jabberwocky.trace.event.FieldValue.ArrayValue
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.lttng.scope.ttt.ctf.CtfTestTrace
import java.util.stream.Collectors
import kotlin.streams.asStream

/**
 * Tests making sure sign extension sign extension of field values works
 * correctly.

 * See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=491382

 * @author Alexandre Montplaisir
 */
class CtfTmfEventFieldSignExtensionTest {

    companion object {
        private lateinit var ETT: ExtractedCtfTestTrace

        @BeforeAll
        @JvmStatic
        fun setupClass() {
            @Suppress("deprecation") // The debug info in this trace is deprecated, but we're testing other things here.
            ETT = ExtractedCtfTestTrace(CtfTestTrace.DEBUG_INFO3)
        }

        @AfterAll
        @JvmStatic
        fun teardownClass() {
            ETT.close()
        }
    }

    /**
     * Test that signed 8-byte integers are printed correctly.
     */
    @Test
    fun testUnsignedByte() {
        /*
         * Third event of the trace is printed like this by Babeltrace:
         *
         * [16:25:03.003427176] (+0.000001578) sonoshee lttng_ust_statedump:build_id:
         *      { cpu_id = 0 }, { ip = 0x7F3BBEDDDE1E, vpid = 3520 },
         *      { baddr = 0x400000, _build_id_length = 20, build_id = [ [0] = 0x1, [1] = 0xC6, [2] = 0x5, [3] = 0xBC, [4] = 0xF3, [5] = 0x8D, [6] = 0x6, [7] = 0x8D, [8] = 0x77, [9] = 0xA6, [10] = 0xE0, [11] = 0xA0, [12] = 0x2C, [13] = 0xED, [14] = 0xE6, [15] = 0xA5, [16] = 0xC, [17] = 0x57, [18] = 0x50, [19] = 0xB5 ] }
         */
        val expectedValues = longArrayOf(0x1, 0xC6, 0x5, 0xBC, 0xF3, 0x8D, 0x6, 0x8D, 0x77, 0xA6, 0xE0, 0xA0, 0x2C, 0xED, 0xE6, 0xA5, 0xC, 0x57, 0x50, 0xB5)

        val expectedToString = expectedValues.asSequence().asStream()
                .map({ i -> "0x" + java.lang.Long.toHexString(i) })
                .collect(Collectors.joining(", ", "[", "]"))

        ETT.trace.iterator().use({ iter ->
            /* Go to third event */
            iter.next()
            iter.next()

            /* Retrieve the event's field called "build_id" */
            val event = iter.next()
            val arrayValue = event.fields["build_id"]?.asType<ArrayValue<IntegerValue>>()!!
            val values = (0 until arrayValue.size)
                    .map { arrayValue.getElement(it) }
                    .map { it.value }
                    .toLongArray()

            assertArrayEquals(expectedValues, values)
            assertEquals(expectedToString, arrayValue.toString())
        })
    }

}
