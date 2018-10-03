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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class TraceIteratorTestBase {

    protected abstract val trace: Trace<TraceEvent>

    protected abstract val event1: TraceEvent
    protected abstract val event2: TraceEvent
    protected abstract val event3: TraceEvent
    protected abstract val timestampBetween1and2: Long

    protected abstract val middleEvent: TraceEvent
    protected abstract val middleEventPosition: Int

    protected abstract val lastEvent: TraceEvent
    protected abstract val timestampAfterEnd: Long

    protected lateinit var iterator: TraceIterator<TraceEvent>
        private set

    @BeforeEach
    fun setup() {
        iterator = trace.iterator()
    }

    @AfterEach
    fun cleanup() {
        iterator.close()
    }

    @Test
    fun testInitial() {
        with(iterator) {
            assertTrue(hasNext())
            assertEquals(event1, next())
            assertEquals(event2, next())
            assertEquals(event3, next())
        }
    }

    // ------------------------------------------------------------------------
    // seek() tests
    // ------------------------------------------------------------------------

    @Test
    fun testSeekBeforeBegin() {
        with(iterator) {
            /* Read some events, then seek back to the beginning */
            repeat(2, { next() })
            seek(0)
            assertEquals(event1, next())
        }
    }

    @Test
    fun testSeekAtBegin() {
        with(iterator) {
            repeat(2, { next() })
            seek(event1.timestamp)
            assertEquals(event1, next())
        }
    }

    @Test
    fun testSeekBetweenEvents() {
        with(iterator) {
            seek(timestampBetween1and2)
            assertEquals(event2, next())
        }
    }

    @Test
    fun testSeekAtEvent() {
        with(iterator) {
            seek(event2.timestamp)
            assertEquals(event2, next())
        }
    }

    @Test
    fun testSeekAtEnd() {
        with(iterator) {
            seek(event3.timestamp)
            assertEquals(event3, next())
        }
    }

    @Test
    fun testSeekAfterEnd() {
        with(iterator) {
            seek(timestampAfterEnd)
            assertFalse(hasNext())
        }
    }

    // ------------------------------------------------------------------------
    // previous()/hasPrevious() tests
    // ------------------------------------------------------------------------

    @Test
    fun testPreviousInitial() {
        assertFalse(iterator.hasPrevious())
    }

    @Test
    fun testPreviousAfterEnd() {
        with(iterator) {
            seek(timestampAfterEnd)
            assertFalse(hasNext())
            assertTrue(hasPrevious())
            assertEquals(lastEvent, previous())
        }
    }

    @Test
    fun testPreviousToBeginning() {
        with(iterator) {
            seek(event3.timestamp)
            assertTrue(hasPrevious())
            previous()
            assertTrue(hasPrevious())
            val event = previous()
            assertEquals(event1, event)
            assertFalse(hasPrevious())

        }
    }

    @Test
    fun testBackAndForth() {
        with(iterator) {
            seek(middleEvent.timestamp)
            repeat(2, { previous() })
            repeat(2, { next() })
            assertEquals(middleEvent, next())

            repeat(2, { next() })
            repeat(2, { previous() })
            assertEquals(middleEvent, previous())
        }
    }

    @Test
    fun testPreviousToBeginningFromMiddle() {
        with(iterator) {
            seek(middleEvent.timestamp)
            var lastReadEvent: TraceEvent? = null
            try {
                repeat(middleEventPosition, {
                    lastReadEvent = previous()
                })
            } catch (e: NoSuchElementException) {
                System.err.println("Last read event: $lastReadEvent")
                throw e
            }
            assertFalse(hasPrevious(), "Last read event: $lastReadEvent")
            assertEquals(event1, lastReadEvent)
        }
    }

    // ------------------------------------------------------------------------
    // copy() tests
    // ------------------------------------------------------------------------

    @Test
    fun testCopyStart() {
        using {
            with(iterator.copy().autoClose()) {
                assertTrue(hasNext())
                assertEquals(event1, next())
                assertEquals(event2, next())
                assertEquals(event3, next())
            }
        }
    }

    @Test
    fun testCopyMiddleNext() {
        iterator.next()
        using { with(iterator.copy().autoClose()) {
            assertTrue(hasNext())
            assertEquals(event2, next())
            assertEquals(event3, next())
        }}
    }

    @Test
    fun testCopyMiddleSeek() {
        iterator.seek(event2.timestamp)
        using { with(iterator.copy().autoClose()) {
            assertTrue(hasNext())
            assertEquals(event2, next())
            assertEquals(event3, next())
        }}
    }

    @Test
    fun testCopyEnd() {
        iterator.seek(timestampAfterEnd)
        using { with(iterator.copy().autoClose()) {
            assertFalse(hasNext())
        }}
    }

    @Test
    fun testCopyOfCopy() {
        using {
            val copy1 = iterator.copy().autoClose()
            val copy2 = copy1.copy().autoClose()

            listOf(copy1, copy2).forEach {
                // deal
                with(it) {
                    assertEquals(event1, next())
                    assertEquals(event2, next())
                    assertEquals(event3, next())
                }
            }
        }
    }

    @Test
    fun testUsedCopyOfCopy() {
        iterator.seek(event2.timestamp)
        using {
            val copy1 = iterator.copy().autoClose()
            val copy2 = copy1.copy().autoClose()

            listOf(copy1, copy2).forEach {
                with(it) {
                    assertEquals(event2, next())
                    assertEquals(event3, next())
                }
            }
        }
    }

    @Test
    fun testSeekAndCopy() {
        using {
            val iter1 = iterator
            iter1.seek(event3.timestamp)
            val iter2 = iter1.copy().autoClose()
            iter1.seek(lastEvent.timestamp)
            iter2.seek(event2.timestamp)

            assertEquals(lastEvent, iter1.next())
            assertEquals(event2, iter2.next())
        }
    }

}