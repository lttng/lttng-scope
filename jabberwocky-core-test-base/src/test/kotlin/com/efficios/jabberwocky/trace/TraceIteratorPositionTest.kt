/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace

import com.efficios.jabberwocky.trace.event.TraceEvent
import com.efficios.jabberwocky.utils.using
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TraceIteratorPositionTest {

    private val trace = TraceStubs.TraceStub1()

    private lateinit var iterator: TraceIterator<TraceEvent>

    @BeforeEach
    fun setup() {
        iterator = trace.iterator()
    }

    @AfterEach
    fun cleanup() {
        iterator.close()
    }

    @Test
    fun testNewFreshIterator() {
        using {
            val pos = TraceIteratorPosition.of(iterator).autoClose()
            val iter2 = pos.newIterator().autoClose()
            verifyIteratorEquals(iterator, iter2)
        }
    }

    @Test
    fun testNewUsedIterator() {
        iterator.seek(trace.events[1].timestamp)
        using {
            val pos = TraceIteratorPosition.of(iterator).autoClose()
            val iter2 = pos.newIterator().autoClose()
            verifyIteratorEquals(iterator, iter2)
        }
    }

    @Test
    fun testMovingInitialIterator() {
        using {
            val pos = TraceIteratorPosition.of(iterator).autoClose()
            val iter2 = pos.newIterator().autoClose()
            iterator.seek(trace.events[1].timestamp)
            /* iter2 should not have moved on its own! */
            iter2.next()
            verifyIteratorEquals(iterator, iter2)
        }
    }

    @Test
    fun testMovingAfterSeek() {
        iterator.seek(trace.events[1].timestamp)
        using {
            val pos = TraceIteratorPosition.of(iterator).autoClose()
            val iter2 = pos.newIterator().autoClose()
            iterator.seek(trace.events[2].timestamp)
            /* iter2 should not have moved on its own! */
            iter2.next()
            verifyIteratorEquals(iterator, iter2)
        }
    }

    private fun verifyIteratorEquals(iter1: TraceIterator<TraceEvent>, iter2: TraceIterator<TraceEvent>) {
        while (iter1.hasNext()) {
            assertEquals(iter1.next(), iter2.next())
        }
        assertFalse(iter1.hasNext())
        assertFalse(iter2.hasNext())
    }
}
