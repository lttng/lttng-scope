/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson

 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 * Contributors:
 * Alexandre Montplaisir - Initial API and implementation
 */

package com.efficios.jabberwocky.ctf.trace.event

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace
import com.efficios.jabberwocky.trace.event.TraceLostEvent
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.lttng.scope.ttt.ctf.CtfTestTrace

/**
 * Tests to verify that lost events are handled correctly.

 * Be wary if you are using Babeltrace to cross-check those values. There could
 * be a bug in Babeltrace with regards to lost events. See
 * http://bugs.lttng.org/issues/589

 * It's not 100% sure at this point which implementation is correct, so for now
 * these tests assume the Java implementation is the right one.

 * @author Alexandre Montplaisir
 */
class CtfTmfLostEventsTest {

    companion object {
        private lateinit var HELLO_LOST_TT: ExtractedCtfTestTrace
        private lateinit var DYNSCOPE_TT: ExtractedCtfTestTrace

        @BeforeAll
        @JvmStatic
        fun setupClass() {
            HELLO_LOST_TT = ExtractedCtfTestTrace(CtfTestTrace.HELLO_LOST)
            DYNSCOPE_TT = ExtractedCtfTestTrace(CtfTestTrace.DYNSCOPE)
        }

        @AfterAll
        @JvmStatic
        fun teardownClass() {
            HELLO_LOST_TT.close()
            DYNSCOPE_TT.close()
        }
    }

    /**
     * Test that the number of events is reported correctly (a range of lost
     * events is counted as one event).
     */
    @Test
    fun testNbEvents() {
        val expectedReal = 32300L
        val expectedLost = 562L

        val req = EventCountRequest(HELLO_LOST_TT.trace)

        assertEquals(expectedReal, req.real)
        assertEquals(expectedLost, req.lost)
    }

    /**
     * Test that the number of events is reported correctly (a range of lost
     * events is counted as one event). Events could be wrongly counted as lost
     * events in certain situations.
     */
    @Test
    fun testNbEventsBug475007() {
        val expectedReal = 100003L
        val expectedLost = 1L

        val req = EventCountRequest(DYNSCOPE_TT.trace)

        assertEquals(expectedReal, req.real)
        assertEquals(expectedLost, req.lost)
    }

    /**
     * Test getting the first lost event from the trace.
     */
    @Test
    fun testFirstLostEvent() {
        val trace = HELLO_LOST_TT.trace
        val rank = 190L
        val start = 1376592664828900165L
        val end = start + 502911L
        val nbLost = 859L

        validateLostEvent(trace, rank, start, end, nbLost)
    }

    /**
     * Test getting the second lost event from the trace.
     */
    @Test
    fun testSecondLostEvent() {
        val trace = HELLO_LOST_TT.trace
        val rank = 229L
        val start = 1376592664829477058L
        val end = start + 347456L
        val nbLost = 488L

        validateLostEvent(trace, rank, start, end, nbLost)
    }

    /**
     * Test getting one normal event from the trace (lost events should not
     * interfere).
     */
    @Test
    fun testNormalEvent() {
        val trace = HELLO_LOST_TT.trace
        val rank = 200L
        val ts = 1376592664829425780L

        val event = getEventAtTimestamp(trace, ts)
        /* Make sure seeking by rank yields the same event */
        val event2 = getEventAtRank(trace, rank)
        assertEquals(event, event2)

        assertFalse(event is TraceLostEvent)
        assertEquals(ts, event.timestamp)
    }

    // ------------------------------------------------------------------------
    // Event requests
    // ------------------------------------------------------------------------

    private class EventCountRequest(trace: CtfTrace) {

        var real: Long = 0
            private set
        var lost: Long = 0
            private set

        init {
            trace.iterator().use { iter ->
                while (iter.hasNext()) {
                    val event = iter.next()
                    if (event is TraceLostEvent) {
                        lost++
                    } else {
                        real++
                    }
                }
            }
        }
    }

    private fun validateLostEvent(trace: CtfTrace, rank: Long, start: Long, end: Long, nbLost: Long) {
        val ev = getLostEventAtTimestamp(trace, start)
        /* Make sure seeking by rank yields the same event */
        val ev2 = getEventAtRank(trace, rank)
        assertEquals(ev, ev2)

        assertTrue(ev is TraceLostEvent)
        val event = ev as TraceLostEvent

        assertEquals(start, event.timestamp)
        assertEquals(start, event.timeRange.startTime)
        assertEquals(end, event.timeRange.endTime)
        assertEquals(nbLost, event.nbLostEvents)
    }

    private fun getEventAtTimestamp(trace: CtfTrace, timestamp: Long): CtfTraceEvent {
        trace.iterator().use({ iter ->
            while (iter.hasNext()) {
                val event = iter.next()
                if (event.timestamp >= timestamp) {
                    return event
                }
            }
        })
        throw IllegalArgumentException("No event with timestamp $timestamp found.")
    }

    private fun getLostEventAtTimestamp(trace: CtfTrace, timestamp: Long): CtfTraceEvent {
        trace.iterator().use({ iter ->
            while (iter.hasNext()) {
                val event = iter.next()
                if (event.timestamp >= timestamp && event is TraceLostEvent) {
                    return event
                }
            }
        })
        throw IllegalArgumentException("No event with timestamp $timestamp found.")
    }

    private fun getEventAtRank(trace: CtfTrace, rank: Long): CtfTraceEvent {
        trace.iterator().use({ iter ->
            for (remaining in rank downTo 1) {
                iter.next()
            }
            return iter.next()
        })
    }
}
