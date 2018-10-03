/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

/**
 * Test suite for the [TimeRange] class.
 */
class TimeRangeTest {

    private val fixture = TimeRange.of(20, 30)

    /**
     * Test that attempting to build a time range with invalid values is forbidden.
     */
    @Test
    fun testBadValues() {
        assertThrows<IllegalArgumentException> { TimeRange.of(20, 10) }
    }

    @Test
    fun testStartTime() {
        assertThat(fixture.startTime).isEqualTo(20)
    }

    @Test
    fun testEndTime() {
        assertThat(fixture.endTime).isEqualTo(30)
    }

    @Test
    fun testDuration() {
        assertThat(fixture.duration).isEqualTo(10)
    }

    @Test
    fun testIsSingleTimestamp() {
        assertThat(fixture.isSingleTimestamp).isFalse()
        assertThat(TimeRange.of(10, 10).isSingleTimestamp)
    }

    @Test
    fun testContains() {
        assertAll(
                { assertThat(23 in fixture) },
                { assertThat(10 !in fixture) },
                { assertThat(50 !in fixture) },

                /* contains() is inclusive */
                { assertThat(20 in fixture) },
                { assertThat(30 in fixture) },
                { assertThat(19 !in fixture) },
                { assertThat(31 !in fixture) }
        )
    }

    /** Time ranges that should intersect 'fixture' */
    private val intersectingRanges = listOf(
            15 to 20,
            15 to 25,
            20 to 25,
            22 to 28,
            25 to 30,
            25 to 35,
            30 to 35,

            15 to 35,
            15 to 30,
            20 to 30,
            20 to 35,

            20 to 20,
            25 to 25,
            30 to 30)
            .map { TimeRange.of(it.first.toLong(), it.second.toLong()) }

    /** Time ranges that should not intersect 'fixture' */
    private val nonIntersectingRanges = listOf(
            12 to 18,
            32 to 38,
            15 to 15,
            35 to 35)
            .map { TimeRange.of(it.first.toLong(), it.second.toLong()) }

    @TestFactory
    fun testIntersects() =
            intersectingRanges.map {
                DynamicTest.dynamicTest("$it should intersect $fixture") {
                    assertThat(it.intersects(fixture))
                }
            }

    @TestFactory
    fun testDoesNotIntersect() =
            nonIntersectingRanges.map {
                DynamicTest.dynamicTest("$it should not intersect $fixture") {
                    assertThat(it.intersects(fixture)).isFalse()
                    assertThat(it.intersection(fixture)).isNull()
                }
            }

    @TestFactory
    fun testIntersection(): List<DynamicTest> {
        val intersections = listOf(
                20 to 20,
                20 to 25,
                20 to 25,
                22 to 28,
                25 to 30,
                25 to 30,
                30 to 30,

                20 to 30,
                20 to 30,
                20 to 30,
                20 to 30,

                20 to 20,
                25 to 25,
                30 to 30)
                .map { TimeRange.of(it.first.toLong(), it.second.toLong()) }


        return intersectingRanges.mapIndexed { index, timeRange ->
            val expected = intersections[index]
            DynamicTest.dynamicTest("Intersection of $timeRange and $fixture should be $expected") {
                assertThat(timeRange.intersection(fixture)).isEqualTo(expected)
            }
        }
    }
}
