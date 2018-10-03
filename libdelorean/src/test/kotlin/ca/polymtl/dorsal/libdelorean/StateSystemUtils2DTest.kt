/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean

import ca.polymtl.dorsal.libdelorean.backend.StateHistoryBackendFactory
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException
import ca.polymtl.dorsal.libdelorean.interval.StateInterval
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class StateSystemUtils2DTest {

    companion object {
        private const val START_TIME = 1000L
        private const val END_TIME = 2200L
        private const val DUMMY_STRING = "test"
    }

    private lateinit var stateSystem: IStateSystemReader
    private var quark1 = -1
    private var quark2 = -1
    private var quark3 = -1
    private var quark4 = -1

    @BeforeEach
    fun setup() {
        try {
            val backend = StateHistoryBackendFactory.createInMemoryBackend(DUMMY_STRING, START_TIME)
            val ssb = StateSystemFactory.newStateSystem(backend)
            quark1 = ssb.getQuarkAbsoluteAndAdd(DUMMY_STRING + "1")
            quark2 = ssb.getQuarkAbsoluteAndAdd(DUMMY_STRING + "2")
            quark3 = ssb.getQuarkAbsoluteAndAdd(DUMMY_STRING + "3")
            quark4 = ssb.getQuarkAbsoluteAndAdd(DUMMY_STRING + "4")

            ssb.modifyAttribute(1200L, StateValue.newValueInt(10), quark1)
            ssb.modifyAttribute(1500L, StateValue.newValueInt(20), quark1)

            (START_TIME until END_TIME step 500).forEachIndexed { index, i ->
                ssb.modifyAttribute(i, StateValue.newValueInt(index), quark2)
            }

            (START_TIME until END_TIME step 200).forEachIndexed { index, i ->
                ssb.modifyAttribute(i, StateValue.newValueInt(index), quark3)
            }

            (START_TIME until END_TIME step 100).forEachIndexed { index, i ->
                ssb.modifyAttribute(i, StateValue.newValueInt(index), quark4)
            }

            ssb.closeHistory(END_TIME)

            stateSystem = ssb

        } catch (e: AttributeNotFoundException) {
            fail(e.message)
        }
    }

    @AfterEach
    fun tearDown() {
        stateSystem.dispose()
    }

    @Test
    fun testDetermineNextQueryTs() {
        val ts1 = determineNextQueryTs(
                intervalFrom(0, 199, 1, null),
                0, 0, 50)
        assertEquals(200, ts1)

        val ts2 = determineNextQueryTs(
                intervalFrom(200, 230, 1, 1),
                0, 200, 50)
        assertEquals(250, ts2)

        val ts3 = determineNextQueryTs(
                intervalFrom(231, 400, 1, 2),
                0, 250, 50)
        assertEquals(450, ts3)
    }

    @Test
    fun testDetermineNextQueryTsWithRangeStart() {
        val ts1 = determineNextQueryTs(
                intervalFrom(100, 199, 1, null),
                100, 100, 50)
        assertEquals(200, ts1)

        val ts2 = determineNextQueryTs(
                intervalFrom(200, 230, 1, 1),
                100, 200, 50)
        assertEquals(250, ts2)

        val ts3 = determineNextQueryTs(
                intervalFrom(231, 400, 1, 2),
                100, 250, 50)
        assertEquals(450, ts3)
    }

    @Test
    fun testEmptySet() {
        val iter = stateSystem.iterator2D(START_TIME + 10, END_TIME - 10, 1, emptySet())
        assertFalse(iter.hasNext())
    }

    @Test
    fun test2DIteratorFullRange() {
        val iter = stateSystem.iterator2D(START_TIME, END_TIME, 1, setOf(quark1, quark2, quark3, quark4))
        val actualIntervals = iter.asSequence()
                .flatMap { it.queryResults.values.asSequence() }
                .sortedWith(compareBy({ it.attribute }, { it.start }))
                .toList()

        /* We should have all the intervals in the state system */
        val expectedIntervals = listOf(
                intervalFrom(START_TIME, 1199, quark1, null),
                intervalFrom(1200, 1499, quark1, 10),
                intervalFrom(1500, END_TIME, quark1, 20),

                intervalFrom(START_TIME, 1499, quark2, 0),
                intervalFrom(1500, 1999, quark2, 1),
                intervalFrom(2000, END_TIME, quark2, 2),

                intervalFrom(START_TIME, 1199, quark3, 0),
                intervalFrom(1200, 1399, quark3, 1),
                intervalFrom(1400, 1599, quark3, 2),
                intervalFrom(1600, 1799, quark3, 3),
                intervalFrom(1800, 1999, quark3, 4),
                intervalFrom(2000, END_TIME, quark3, 5),

                intervalFrom(START_TIME, 1099, quark4, 0),
                intervalFrom(1100, 1199, quark4, 1),
                intervalFrom(1200, 1299, quark4, 2),
                intervalFrom(1300, 1399, quark4, 3),
                intervalFrom(1400, 1499, quark4, 4),
                intervalFrom(1500, 1599, quark4, 5),
                intervalFrom(1600, 1699, quark4, 6),
                intervalFrom(1700, 1799, quark4, 7),
                intervalFrom(1800, 1899, quark4, 8),
                intervalFrom(1900, 1999, quark4, 9),
                intervalFrom(2000, 2099, quark4, 10),
                intervalFrom(2100, END_TIME, quark4, 11)
        )
        assertEquals(expectedIntervals, actualIntervals)
    }

    @Test
    fun test2DIteratorRange() {
        val rangeStart: Long = 1100
        val rangeEnd:Long = 1900
        val iter = stateSystem.iterator2D(rangeStart, rangeEnd, 1, setOf(quark1, quark2, quark3))
        val actualIntervals = iter.asSequence()
                .flatMap { it.queryResults.values.asSequence() }
                .sortedWith(compareBy({ it.attribute }, { it.start }))
                .toList()

        /* Only quark 1, 2 and 3, and only intervals inside the time range */
        val expectedIntervals = listOf(
                intervalFrom(START_TIME, 1199, quark1, null),
                intervalFrom(1200, 1499, quark1, 10),
                intervalFrom(1500, END_TIME, quark1, 20),

                intervalFrom(START_TIME, 1499, quark2, 0),
                intervalFrom(1500, 1999, quark2, 1),

                intervalFrom(START_TIME, 1199, quark3, 0),
                intervalFrom(1200, 1399, quark3, 1),
                intervalFrom(1400, 1599, quark3, 2),
                intervalFrom(1600, 1799, quark3, 3),
                intervalFrom(1800, 1999, quark3, 4)
        )
        assertEquals(expectedIntervals, actualIntervals)
    }

    @Test
    fun test2DIteratorResolution() {
        val rangeStart: Long = 1100
        val rangeEnd:Long = 1900
        val iter = stateSystem.iterator2D(rangeStart, rangeEnd, 100, setOf(quark1, quark2, quark3, quark4))
        val actualIntervals = iter.asSequence()
                .flatMap { it.queryResults.values.asSequence() }
                .sortedWith(compareBy({ it.attribute }, { it.start }))
                .toList()

        /*
         * Only keep intervals that cross TWO consecutive points between the following:
         * 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900
         */
        val expectedIntervals = listOf(
                intervalFrom(1200, 1499, quark1, 10),
                intervalFrom(1500, END_TIME, quark1, 20),

                intervalFrom(START_TIME, 1499, quark2, 0),
                intervalFrom(1500, 1999, quark2, 1),

                intervalFrom(1200, 1399, quark3, 1),
                intervalFrom(1400, 1599, quark3, 2),
                intervalFrom(1600, 1799, quark3, 3),
                intervalFrom(1800, 1999, quark3, 4)
        )
        assertEquals(expectedIntervals, actualIntervals)
    }

    @Test
    fun testIterator2DBounds() {
        val iter = stateSystem.iterator2D(START_TIME, 1550, 500, setOf(quark3, quark4))
        val actualIntervals = iter.asSequence()
                .flatMap { it.queryResults.values.asSequence() }
                .sortedWith(compareBy({ it.attribute }, { it.start }))
                .toList()

        /* Resolution points are 1000, 1500, 1550 (rangeEnd) */
        val expectedIntervals = listOf(
                intervalFrom(1400, 1599, quark3, 2),
                intervalFrom(1500, 1599, quark4, 5)
        )
        assertEquals(expectedIntervals, actualIntervals)
    }

    private fun intervalFrom(start: Long, end: Long, quark: Int, intStateValue: Int?): StateInterval =
            StateInterval(start, end, quark,
                    if (intStateValue == null) StateValue.nullValue() else StateValue.newValueInt(intStateValue))
}
