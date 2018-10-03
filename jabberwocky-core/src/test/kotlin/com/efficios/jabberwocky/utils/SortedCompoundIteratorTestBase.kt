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

/**
 * Base test class for compound iterator tests.
 */
abstract class SortedCompoundIteratorTestBase {

    protected val list1 = listOf(1, 3, 5)
    protected val list2 = listOf(2, 4, 6)
    protected val list3 = listOf(4, 5, 6)
    protected val list4 = listOf(2, 2, 2)

    protected val customList1 = listOf(
            MyObject("A"),
            MyObject("B"),
            MyObject("C"))

    protected val customList2 = listOf(
            MyObject("a"),
            MyObject("b"),
            MyObject("c"))


    protected abstract fun <T> provideCompoundIterator(iterators: Collection<Iterator<T>>, comparator: Comparator<T>): Iterator<T>

    /**
     * Test that an error is correctly reported if there are less than 1
     * iterator.
     */
    @Test
    fun testNoIter() {
        assertThrows<IllegalArgumentException> {
            provideCompoundIterator(emptyList<Iterator<String>>(), naturalOrder())
        }
    }

    /**
     * Test a compound iterator with just 1 iterator, basically just a wrapper.
     */
    @Test
    fun test1Iter() {
        val iters = listOf(list1.iterator())
        val sci = provideCompoundIterator(iters, naturalOrder())
        assertTrue(sci.hasNext())
        testIteratorContents(sci, 1, 3, 5)
    }

    /**
     * Test a compound iterator with 2 iterators, requiring to sort the values.
     */
    @Test
    fun test2Iters() {
        val iters = listOf(list1, list2).map { it.iterator() }
        val sci = provideCompoundIterator(iters, naturalOrder())
        assertTrue(sci.hasNext())
        testIteratorContents(sci, 1, 2, 3, 4, 5, 6)
    }

    /**
     * Test that identical values coming from the same iterator are correctly
     * reported in the global iteration.
     */
    @Test
    fun testIdenticalValues1Iter() {
        val iters = listOf(list1, list4).map { it.iterator() }
        val sci = provideCompoundIterator(iters, naturalOrder())
        testIteratorContents(sci, 1, 2, 2, 2, 3, 5)
    }

    /**
     * Test a compound iterator with separate iterators that hold identical
     * (compareTo() == 0) values. The duplicate values should be present in the
     * iteration.
     */
    @Test
    fun testIdenticalValues2Iters() {
        val iters = listOf(list2, list3).map { it.iterator() }
        val sci = provideCompoundIterator(iters, naturalOrder())
        testIteratorContents(sci, 2, 4, 4, 5, 6, 6)
    }

    /**
     * Test that upon exhaustion, the iterator correctly throws a
     * [NoSuchElementException] if we try to read from it.
     */
    @Test
    fun testExhaustion() {
        val iter1 = list1.iterator()
        val iters = listOf(iter1)
        val sci = provideCompoundIterator(iters, naturalOrder())

        sci.next()
        sci.next()
        sci.next()
        assertThrows<NoSuchElementException> { sci.next() }
    }

    // ------------------------------------------------------------------------
    // Tests with custom objects and comparators
    // ------------------------------------------------------------------------

    protected data class MyObject(val value: String)

    /**
     * Test a compound iterator of custom objects
     */
    @Test
    fun testCustomObjects1() {
        val iters = listOf(customList1, customList2).map { it.iterator() }
        val sci = provideCompoundIterator(iters, compareBy { it.value })

        /* Default string comparator places capitals first */
        testIteratorContents(sci,
                MyObject("A"),
                MyObject("B"),
                MyObject("C"),
                MyObject("a"),
                MyObject("b"),
                MyObject("c"))
    }

    /**
     * Test a compound iterator of custom objects with a more complex
     * comparator.
     */
    @Test
    fun testCustomObjects2() {
        val iters = listOf(customList2, customList1).map { it.iterator() }
        val comparator = Comparator<MyObject> { o1, o2 -> o1.value.compareTo(o2.value, ignoreCase = true) }
        val sci = provideCompoundIterator(iters, comparator)

        /*
         * Using custom comparator here. Note we put the contents of customList2
         * first, so they will show up first in the compound iterator.
         */
        testIteratorContents(sci,
                MyObject("a"),
                MyObject("A"),
                MyObject("b"),
                MyObject("B"),
                MyObject("c"),
                MyObject("C"))
    }

    @SafeVarargs
    private fun <T> testIteratorContents(iter: Iterator<T>, vararg values: T) {
        for (value in values) {
            assertEquals(value, iter.next())
        }
        assertFalse(iter.hasNext())
    }

}
