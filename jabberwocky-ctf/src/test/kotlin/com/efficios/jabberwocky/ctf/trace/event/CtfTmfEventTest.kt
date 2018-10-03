/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson

 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 * Contributors:
 * Matthew Khouzam - Initial generation with CodePro tools
 * Alexandre Montplaisir - Clean up, consolidate redundant tests
 * Patrick Tasse - Remove getSubField
 */

package com.efficios.jabberwocky.ctf.trace.event

import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.lttng.scope.ttt.ctf.CtfTestTrace

/**
 * The class `CtfTmfEventTest` contains tests for the class
 * `[CtfTmfEvent]`.
 *
 * @author ematkho
 *
 */
class CtfTmfEventTest {

    companion object {
        private lateinit var ETT: ExtractedCtfTestTrace

        @BeforeAll
        @JvmStatic
        fun setupClass() {
            ETT = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)
        }

        @AfterAll
        @JvmStatic
        fun teardownClass() {
            ETT.close()
        }

        private const val VALID_FIELD = "ret"
    }

    /**
     * <pre>
     * babeltrace output :
     * [11:24:42.440133097] (+?.?????????) sys_socketcall: { cpu_id = 1 }, { call = 17, args = 0xB7555F30 }
     * [11:24:42.440137077] (+0.000003980) exit_syscall: { cpu_id = 1 }, { ret = 4132 }
     * </pre>
     */

    private lateinit var fixture: CtfTraceEvent

    /**
     * Perform pre-test initialization.
     */
    @BeforeEach
    fun setUp() {
        val trace = ETT.trace
        trace.iterator().use({ iter ->
            /* This test uses the second event of the trace */
            iter.next()
            fixture = iter.next()
        })
    }

    /**
     * Run the CTFEvent(EventDefinition,StreamInputReader) constructor test.
     */
    @Test
    fun testCTFEvent_read() {
        assertNotNull(fixture)
    }

    /**
     * Run the int getCPU() method test.
     */
    @Test
    fun testGetCPU() {
        val result = fixture.cpu
        assertEquals(1, result)
    }

    /**
     * Run the String getEventName() method test.
     */
    @Test
    fun testGetEventName() {
        val result = fixture.eventName
        assertEquals("exit_syscall", result)
    }

    /**
     * Run the ArrayList<String> getFieldNames() method test.
    </String> */
    @Test
    fun testGetFieldNames() {
        val result = fixture.fields.keys
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    /**
     * Run the Object getFieldValue(String) method test.
     */
    @Test
    fun testGetFieldValue() {
        val result = fixture.fields[VALID_FIELD]?.asType<IntegerValue>()!!
        assertNotNull(result.value)
    }

    /**
     * Run the long getTimestamp() method test.
     */
    @Test
    fun testGetTimestamp() {
        val result = fixture.timestamp
        assertEquals(1332170682440137077L, result)
    }

    /**
     * Test the custom CTF attributes methods. The test trace doesn't have any,
     * so the list of attributes should be empty.
     */
    @Test
    fun testGetAttributes() {
        val attributes = fixture.attributes
        assertNotNull(attributes)
        assertTrue(attributes.isEmpty())
    }

}
