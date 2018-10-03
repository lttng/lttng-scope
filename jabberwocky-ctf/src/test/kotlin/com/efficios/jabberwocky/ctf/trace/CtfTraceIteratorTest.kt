/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.trace.TraceIteratorTestBase
import com.efficios.jabberwocky.trace.event.FieldValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.lttng.scope.ttt.ctf.CtfTestTrace

class CtfTraceIteratorTest : TraceIteratorTestBase() {

    companion object {
        private lateinit var ETT: ExtractedCtfTestTrace

        @BeforeAll
        @JvmStatic
        fun setupClass() {
            ETT = ExtractedCtfTestTrace(CtfTestTrace.TRACE2)
        }

        @AfterAll
        @JvmStatic
        fun teardownClass() {
            ETT.close()
        }

        private const val TRACE_NB_EVENTS = 595641
    }

    override val trace = ETT.trace

    override val event1 = CtfTraceEvent(trace, 1331668247314038062, 0, "sched_stat_runtime",
            mapOf("comm" to FieldValue.StringValue("lttng-sessiond"),
                    "tid" to FieldValue.IntegerValue(2175),
                    "runtime" to FieldValue.IntegerValue(297955),
                    "vruntime" to FieldValue.IntegerValue(525083943)
            )
    )

    override val event2 = CtfTraceEvent(trace, 1331668247314044708, 0, "sched_stat_wait",
            mapOf("comm" to FieldValue.StringValue("lttng-consumerd"),
                    "tid" to FieldValue.IntegerValue(2193),
                    "delay" to FieldValue.IntegerValue(297955)
            )
    )

    override val event3 = CtfTraceEvent(trace, 1331668247314046266, 0, "sched_switch",
            mapOf("prev_comm" to FieldValue.StringValue("lttng-sessiond"),
                    "prev_tid" to FieldValue.IntegerValue(2175),
                    "prev_prio" to FieldValue.IntegerValue(20),
                    "prev_state" to FieldValue.IntegerValue(1),
                    "next_comm" to FieldValue.StringValue("lttng-consumerd"),
                    "next_tid" to FieldValue.IntegerValue(2193),
                    "next_prio" to FieldValue.IntegerValue(20)
            )
    )

    override val timestampBetween1and2 = event1.timestamp + 100

    override val middleEvent = CtfTraceEvent(trace, 1331668249_621547507, 1, "sys_gettimeofday",
            mapOf("tv" to FieldValue.IntegerValue(0x7F394B3CDD60, 16),
                    "tz" to FieldValue.IntegerValue(0x0, 16)
            )
    )

    /** Remember, first event -> position = 0 */
    override val middleEventPosition = 50_000 - 2

    override val lastEvent = CtfTraceEvent(trace, 1331668259054285979, 0, "sys_ioctl",
            mapOf("fd" to FieldValue.IntegerValue(20),
                    "cmd" to FieldValue.IntegerValue(63059),
                    "arg" to FieldValue.IntegerValue(0)
            )
    )

    override val timestampAfterEnd = lastEvent.timestamp + 100

    @Test
    fun testBackwardsCache() {
        with(iterator as CtfTraceIterator) {
            seek(middleEvent.timestamp)
            assertNull(cacheIterator)

            previous()
            val iter = cacheIterator!!
            assertNotNull(iter)

            /* Exhaust the iterator and see how many events we had in cache */
            var i = 0
            while (iter.hasPrevious()) {
                previous()
                i++
            }
            // TODO Check the actual packet start times and test against the exact value.
            assertThat(i).isLessThan(5000).withFailMessage("Number of events in cache")
        }
    }
}
