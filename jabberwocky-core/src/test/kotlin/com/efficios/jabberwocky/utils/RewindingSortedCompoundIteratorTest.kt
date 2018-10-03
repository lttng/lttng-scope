/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.NoSuchElementException

class RewindingSortedCompoundIteratorTest : SortedCompoundIteratorTestBase() {

    /* Wrapper for base class tests. Ensure the rewinding iterator works the same as a forwards one. */
    private class ForwardIteratorWrapper<out T>(iterator: Iterator<T>) : Iterator<T> by iterator, RewindingIterator<T> {
        override fun hasPrevious() = false
        override fun previous() = throw NoSuchElementException()
    }

    /* Wrapper for ListIterator -> RewindingIterator */
    private class BackwardsIteratorWrapper<out T>(private val listIter: ListIterator<T>) : RewindingIterator<T> {
        override fun hasPrevious() = listIter.hasPrevious()
        override fun hasNext() = listIter.hasNext()
        override fun previous() = listIter.previous()
        override fun next() = listIter.next()
    }

    override fun <T> provideCompoundIterator(iterators: Collection<Iterator<T>>, comparator: Comparator<T>): RewindingIterator<T> {
        return RewindingSortedCompoundIterator(iterators.map { ForwardIteratorWrapper(it) }, comparator)
    }

    private fun <T> provideRewindingIterator(iterators: Collection<ListIterator<T>>, comparator: Comparator<T>): RewindingIterator<T> {
        return RewindingSortedCompoundIterator(iterators.map { BackwardsIteratorWrapper(it) }, comparator)
    }

    // ------------------------------------------------------------------------
    // Basic backwards-seeking tests
    // ------------------------------------------------------------------------

    @Test
    fun test1IterBackwardsInitialState() {
        val iters = listOf(list1.listIterator())
        val sci = provideRewindingIterator(iters, naturalOrder())
        assertTrue(sci.hasNext())
        assertFalse(sci.hasPrevious())
    }

    /**
     * Test a compound iterator with just 1 iterator, basically just a wrapper.
     */
    @Test
    fun test1IterBackwards() {
        val iters = listOf(list1.listIterator(list1.size))
        val sci = provideRewindingIterator(iters, naturalOrder())
        assertFalse(sci.hasNext())
        assertTrue(sci.hasPrevious())
        testBackwardsIteratorContents(sci, 5, 3, 1)
    }

    /**
     * Test a compound iterator with 2 iterators, requiring to sort the values.
     */
    @Test
    fun test2ItersBackwards() {
        val iters = listOf(list1, list2).map { it.listIterator(it.size) }
        val sci = provideRewindingIterator(iters, naturalOrder())
        assertFalse(sci.hasNext())
        assertTrue(sci.hasPrevious())
        testBackwardsIteratorContents(sci, 6, 5, 4, 3, 2, 1)
    }

    /**
     * Test that identical values coming from the same iterator are correctly
     * reported in the global iteration.
     */
    @Test
    fun testIdenticalValues1IterBackwards() {
        val iters = listOf(list1, list4).map { it.listIterator(it.size) }
        val sci = provideRewindingIterator(iters, naturalOrder())
        testBackwardsIteratorContents(sci, 5, 3, 2, 2, 2, 1)
    }

    /**
     * Test a compound iterator with separate iterators that hold identical
     * (compareTo() == 0) values. The duplicate values should be present in the
     * iteration.
     */
    @Test
    fun testIdenticalValues2ItersBackwards() {
        val iters = listOf(list2, list3).map { it.listIterator(it.size) }
        val sci = provideRewindingIterator(iters, naturalOrder())
        testBackwardsIteratorContents(sci, 6, 6, 5, 4, 4, 2)
    }

    /**
     * Test that upon exhaustion, the iterator correctly throws a
     * [NoSuchElementException] if we try to read from it.
     */
    @Test
    fun testExhaustionBackwards() {
        val iters = listOf(list1.listIterator(list1.size))
        val sci = provideRewindingIterator(iters, naturalOrder())

        sci.previous()
        sci.previous()
        sci.previous()
        assertThrows<NoSuchElementException> { sci.previous() }
    }

    // ------------------------------------------------------------------------
    // Tests with custom objects and comparators
    // ------------------------------------------------------------------------

    /**
     * Test a compound iterator of custom objects
     */
    @Test
    fun testCustomObjects1Backwards() {
        val iters = listOf(customList1, customList2).map { it.listIterator(it.size) }
        val sci = provideRewindingIterator(iters, compareBy { it.value })

        /* Default string comparator places capitals first */
        testBackwardsIteratorContents(sci,
                MyObject("c"),
                MyObject("b"),
                MyObject("a"),
                MyObject("C"),
                MyObject("B"),
                MyObject("A"))
    }

    /**
     * Test a compound iterator of custom objects with a more complex
     * comparator.
     */
    @Test
    fun testCustomObjects2Backwards() {
        val iters = listOf(customList2, customList1).map { it.listIterator(it.size) }
        val comparator = Comparator<MyObject> { o1, o2 -> o1.value.compareTo(o2.value, ignoreCase = true) }
        val sci = provideRewindingIterator(iters, comparator)

        /*
         * Using custom comparator here. Note we put the contents of customList2
         * first, so they will show up first in the compound iterator.
         */
        testBackwardsIteratorContents(sci,
                MyObject("c"),
                MyObject("C"),
                MyObject("b"),
                MyObject("B"),
                MyObject("a"),
                MyObject("A"))
    }

    @SafeVarargs
    private fun <T> testBackwardsIteratorContents(iter: RewindingIterator<T>, vararg values: T) {
        for (value in values) {
            assertEquals(value, iter.previous())
        }
        assertFalse(iter.hasPrevious())
    }

    // ------------------------------------------------------------------------
    // Back-and-forth tests
    // ------------------------------------------------------------------------

    @Test
    fun testBackAndForth1Iter() {
        val iters = listOf(list1.listIterator())
        val sci = provideRewindingIterator(iters, naturalOrder())

        with(sci) {
            assertTrue(hasNext())
            assertFalse(hasPrevious())

            assertEquals(1, next())
            assertEquals(3, next())
            assertEquals(3, previous())
            assertEquals(3, next())
            assertEquals(5, next())
            assertFalse(hasNext())

            assertEquals(5, previous())
            assertEquals(3, previous())
            assertEquals(1, previous())
            assertFalse(hasPrevious())

            repeat(3, { next() })
            assertFalse(hasNext())
            assertTrue(hasPrevious())
            assertEquals(5, previous())
        }
    }

    @Test
    fun testBackAndForth2Iters() {
        val iters = listOf(list1, list2).map { it.listIterator() }
        val sci = provideRewindingIterator(iters, naturalOrder())

        with(sci) {
            assertTrue(hasNext())
            assertFalse(hasPrevious())

            assertEquals(1, next())
            assertEquals(2, next())
            assertEquals(3, next())

            assertEquals(3, previous())
            assertEquals(2, previous())
            assertEquals(1, previous())
            assertFalse(hasPrevious())

            repeat(6, { next() })
            assertFalse(hasNext())
            assertTrue(hasPrevious())
            assertEquals(6, previous())
        }
    }
}