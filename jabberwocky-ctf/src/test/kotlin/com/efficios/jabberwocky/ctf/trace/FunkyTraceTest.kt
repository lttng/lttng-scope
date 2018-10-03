/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson

 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 * Contributors:
 * Alexandre Montplaisir - Initial API and implementation
 */

package com.efficios.jabberwocky.ctf.trace

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.trace.event.FieldValue.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.lttng.scope.ttt.ctf.CtfTestTrace

/**
 * More advanced CTF tests using "funky_trace", a trace generated with the
 * Babeltrace CTF writer API, which has lots of fun things like different
 * integer/float sizes and non-standard struct alignments.

 * @author Alexandre Montplaisir
 */
class FunkyTraceTest {

    companion object {
        private lateinit var ETT: ExtractedCtfTestTrace

        @BeforeAll
        @JvmStatic
        fun setupClass() {
            ETT = ExtractedCtfTestTrace(CtfTestTrace.FUNKY_TRACE)
        }

        @AfterAll
        @JvmStatic
        fun teardownClass() {
            ETT.close()
        }

        const val DELTA = 0.0000001
    }

    /**
     * Verify the contents of the first event
     */
    @Test
    fun testFirstEvent() {
        val event = getEvent(0)
        assertEquals("Simple Event", event.eventName)
        assertEquals(1234567L, event.timestamp)
        assertEquals(42L, event.fields["integer_field"]?.asType<IntegerValue>()!!.value)
        assertEquals(3.1415, event.fields["float_field"]?.asType<FloatValue>()!!.value, DELTA)
    }

    /**
     * Verify the contents of the second event (the first "spammy event")
     */
    @Test
    fun testSecondEvent() {
        val event = getEvent(1)
        assertEquals("Spammy_Event", event.eventName)
        assertEquals(1234568L, event.timestamp)
        assertEquals(0L, event.fields["field_1"]?.asType<IntegerValue>()!!.value)
        assertEquals("This is a test", event.fields["a_string"]?.asType<StringValue>()!!.value)
    }

    /**
     * Verify the contents of the last "spammy event"
     */
    @Test
    fun testSecondToLastEvent() {
        val event = getEvent(100000)
        assertEquals("Spammy_Event", event.eventName)
        assertEquals(1334567L, event.timestamp)

        assertEquals(99999L, event.fields["field_1"]?.asType<IntegerValue>()!!.value)
        assertEquals("This is a test", event.fields["a_string"]?.asType<StringValue>()!!.value)
    }

    /**
     * Verify the contents of the last, complex event
     */
    @Test
    fun testLastEvent() {
        /*
         * Last event as seen in Babeltrace:
         * [19:00:00.001334568] (+0.000000001) Complex Test Event: { }, {
         *     uint_35 = 0xDDF00D,
         *     int_16 = -12345,
         *     complex_structure = {
         *         variant_selector = ( INT16_TYPE : container = 1 ),
         *         a_string = "Test string",
         *         variant_value = { INT16_TYPE = -200 },
         *         inner_structure = {
         *             seq_len = 0xA,
         *             a_sequence = [ [0] = 4, [1] = 3, [2] = 2, [3] = 1, [4] = 0, [5] = -1, [6] = -2, [7] = -3, [8] = -4, [9] = -5 ]
         *         }
         *     }
         * }
         */

        val event = getEvent(100001)
        assertEquals("Complex Test Event", event.eventName)
        assertEquals(1334568L, event.timestamp)
        assertEquals(0xddf00dL, event.fields["uint_35"]?.asType<IntegerValue>()!!.value)
        assertEquals(-12345L, event.fields["int_16"]?.asType<IntegerValue>()!!.value)

        val complexStruct = event.fields["complex_structure"] as StructValue
        val fieldNames = complexStruct.elements.keys
        assertTrue(fieldNames.contains("variant_selector"))
        assertTrue(fieldNames.contains("a_string"))
        assertTrue(fieldNames.contains("variant_value"))

        val enumVal = complexStruct.elements["variant_selector"] as EnumValue
        assertEquals("INT16_TYPE", enumVal.stringValue)
        assertEquals(1L, enumVal.longValue)

        assertEquals("Test string", complexStruct.elements["a_string"]?.asType<StringValue>()!!.value)

        val variantField = complexStruct.elements["variant_value"] as IntegerValue
        assertEquals(-200L, variantField.value)

        val innerStruct = complexStruct.elements["inner_structure"] as StructValue
        assertEquals(10L, innerStruct.elements["seq_len"]?.asType<IntegerValue>()!!.value)

        // FIXME Replace Class<?> parameters with something better
        val arrayVal = innerStruct.elements["a_sequence"]?.asType<ArrayValue<IntegerValue>>()!!
        val expectedValues = longArrayOf(4, 3, 2, 1, 0, -1, -2, -3, -4, -5)
        for (i in expectedValues.indices) {
            assertEquals(expectedValues[i], arrayVal.getElement(i).value)
        }
    }

    // ------------------------------------------------------------------------
    // Private stuff
    // ------------------------------------------------------------------------

    private fun getEvent(index: Long): CtfTraceEvent {
        ETT.trace.iterator().use {
            for (remaining in index downTo 1) {
                it.next()
            }
            return it.next()
        }
    }

}
