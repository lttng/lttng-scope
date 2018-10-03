/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.views.timegraph.threads

import ca.polymtl.dorsal.libdelorean.StateSystemUtils
import ca.polymtl.dorsal.libdelorean.interval.StateInterval
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue
import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.lttng.kernel.analysis.os.Attributes
import com.efficios.jabberwocky.lttng.testutils.ExtractedCtfTestTrace
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.timegraph.model.render.states.MultiStateInterval
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateInterval
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.lttng.scope.ttt.ctf.CtfTestTrace
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

// FIXME Tag with "slow" instead?
@Disabled
class ThreadsModelProviderTest {

    companion object {
        private lateinit var TEST_TRACE: ExtractedCtfTestTrace

        @BeforeAll
        @JvmStatic
        fun setupClass() {
            TEST_TRACE = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)
        }

        @AfterAll
        @JvmStatic
        fun teardownClass() {
            TEST_TRACE.close()
        }

        private const val PROJECT_NAME = "test-proj"
        private const val NANOS_PER_SECOND = 1000000000L
    }

    private val provider = ThreadsModelProvider()

    init {
        provider.disableFilterMode(0)
    }

    private lateinit var projectPath: Path

    @BeforeEach
    fun setup() {
        try {
            projectPath = Files.createTempDirectory(PROJECT_NAME)
        } catch (e: IOException) {
            fail<Any>(e.message)
        }

        provider.traceProject = TraceProject.ofSingleTrace(PROJECT_NAME, projectPath, TEST_TRACE.trace)
    }

    @AfterEach
    fun teardown() {
        provider.traceProject = null
        projectPath.toFile().deleteRecursively()
    }

    @Test
    fun test1sStart() {
        val start = provider.traceProject!!.startTime
        val end = start + NANOS_PER_SECOND
        test1s(TimeRange.of(start, end))
    }

    @Test
    fun test1sMiddle() {
        val projStart = provider.traceProject!!.startTime
        val projEnd = provider.traceProject!!.endTime
        val start = sequenceOf(projStart, projEnd).average().toLong()
        val end = start + NANOS_PER_SECOND
        test1s(TimeRange.of(start, end))
    }

    @Test
    fun test1sEnd() {
        val end = provider.traceProject!!.endTime
        val start = end - NANOS_PER_SECOND
        test1s(TimeRange.of(start, end))
    }

    /**
     * Check that the info in a render for the first second of the trace matches
     * the corresponding info found in the state system.
     */
    private fun test1s(range: TimeRange) {
        val ss = provider.stateSystem!!

        /* Check that the list of attributes (tree render) are the same */
        val treeRender = provider.getTreeRender()
        val treeElems = treeRender.allTreeElements

        val tidsFromRender = treeElems
                .filter { it is ThreadsTreeElement }.map { it as ThreadsTreeElement }
                .map { it.tid }
                .map { it.toString() }
                .sorted()

        val threadsQuark = ss.getQuarkAbsolute(Attributes.THREADS)
        val tidsFromSS = ss.getSubAttributes(threadsQuark, false)
                .map { ss.getAttributeName(it) }
                .map { name ->
                    if (name.startsWith(Attributes.THREAD_0_PREFIX)) {
                        "0"
                    } else {
                        name
                    }
                }
                .sorted()

        assertEquals(tidsFromSS, tidsFromRender)

        /* Check that the state intervals are the same */
        testIntervalsResolution(range, 1)
    }

    @Test
    fun testIntervalsResolutionStart() {
        val start = provider.traceProject!!.startTime
        val end = provider.traceProject!!.endTime - (4 * NANOS_PER_SECOND)
        testIntervalsResolution(TimeRange.of(start, end), 10_000_000)
    }

    @Test
    fun testIntervalsResolutionMiddle() {
        val start = provider.traceProject!!.startTime + (2 * NANOS_PER_SECOND)
        val end = provider.traceProject!!.endTime - (2 * NANOS_PER_SECOND)
        testIntervalsResolution(TimeRange.of(start, end), 10_000_000)
    }

    @Test
    fun testIntervalsResolutionEnd() {
        val start = provider.traceProject!!.startTime + (4 * NANOS_PER_SECOND)
        val end = provider.traceProject!!.endTime
        testIntervalsResolution(TimeRange.of(start, end), 10_000_000)
    }

    private fun testIntervalsResolution(range: TimeRange,
                                        resolution: Long) {

        val ss = provider.stateSystem!!
        val threadsQuark = ss.getQuarkAbsolute(Attributes.THREADS)

        val tids = ss.getSubAttributes(threadsQuark, false)
                .map(ss::getAttributeName)
                .sorted()

        for (tid in tids) {
            /* Get the intervals straight from the SS (the reference) */
            val threadQuark = ss.getQuarkRelative(threadsQuark, tid)
            val intervalsFromSS =
                    StateSystemUtils.queryHistoryRange(ss, threadQuark, range.startTime, range.endTime)

            /* Shortcut when resolution = 1 */
            val matchingIntervalsFromSS = if (resolution == 1L) {
                intervalsFromSS
            } else {
                val resolutionPoints = (range.startTime..range.endTime step resolution).toMutableList()
                /* The end time is also an expected resolution point */
                resolutionPoints.add(range.endTime)
                val sortedIntervals = sortedSetOf<StateInterval>(compareBy { it.end })
                sortedIntervals.addAll(intervalsFromSS)
                val ret = mutableListOf<StateInterval>()

                /* Only keep intervals that cross two consecutive resolution points */
                for (i in 1 until resolutionPoints.size) {
                    val point1 = resolutionPoints[i - 1]
                    val point2 = resolutionPoints[i]

                    val target = StateInterval(0, point2, 0, StateValue.nullValue())
                    val toAdd = sortedIntervals.tailSet(target)
                            .filter { it.intersects(point1) && it.intersects(point2) }
                            .singleOrNull()
                    if (toAdd != null && ret.lastOrNull() != toAdd) {
                        ret.add(toAdd)
                    }
                }
                ret
            }

            /* Compare with the intervals obtained from the render provider */
            val elem = provider.getTreeRender().allTreeElements
                    .filter { it is ThreadsTreeElement }.map { it as ThreadsTreeElement }
                    .filter { it.sourceQuark == threadQuark }
                    .single()


            val stateRender = provider.stateProvider.getStateRender(elem, range, resolution, null)
            val intervalsFromRender = stateRender.stateIntervals
                    /* Filter out the multi-states, the SS intervals won't have them */
                    .filter { it !is MultiStateInterval }

            verifySameIntervals(matchingIntervalsFromSS, intervalsFromRender)
        }
    }

    /**
     * Verify that for a known time range, all generated intervals are
     * contiguous but of a different states (multi-states are included in
     * there).
     */
    @Test
    fun testMultiStates() {
        val range = TimeRange.of(1332170683505733202L, 1332170683603572392L)
        val treeElemName = "0/0 - swapper"
        val viewWidth: Long = 1000
        val resolution = range.duration / viewWidth

        val treeElem = provider.getTreeRender().allTreeElements
                .filter { it.name == treeElemName }
                .single()
        val stateRender = provider.stateProvider.getStateRender(treeElem, range, resolution, null)
        val intervals = stateRender.stateIntervals

        assertTrue(intervals.size > 2)

        for (i in 1 until intervals.size) {
            val interval1 = intervals[i - 1]
            val interval2 = intervals[i]

            assertEquals(interval1.endTime + 1, interval2.startTime)
            assertNotEquals(interval1.stateName, interval2.stateName)
        }
    }

    /**
     * Make sure that if multi-states are present at the beginning or end of a
     * time graph render, they actually start/end at the same timestamps as the
     * full state model.
     */
    @Test
    fun testBounds() {
        val project = provider.traceProject!!
        val ss = provider.stateSystem!!
        /*
         * Note that here, the range of the query is the full range of the
         * trace, so the start/end times of the full state system should match
         * the ones in the model. This might not always be the case with
         * multi-states at the beginning/end, since those may have synthetic
         * start/end times.
         */
        val range = TimeRange.of(project.startTime, project.endTime)
        val treeElemName = "0/0 - swapper"
        val viewWidth: Long = 1000
        val resolution = range.duration / viewWidth

        /* Get the intervals from the model */
        val treeElem = provider.getTreeRender().allTreeElements
                .filter { it.name == treeElemName }
                .single()
        val stateRender = provider.stateProvider.getStateRender(treeElem, range, resolution, null)
        val intervalsFromRender = stateRender.stateIntervals

        /* Get the intervals from the state system */
        val threadsQuark = ss.getQuarkAbsolute(Attributes.THREADS)
        val threadQuark = ss.getQuarkRelative(threadsQuark, "0_0")
        val intervalsFromSS = StateSystemUtils.queryHistoryRange(ss, threadQuark, range.startTime, range.endTime)

        /* Check that the first intervals start at the same timestamp. */
        val modelStart = intervalsFromRender.first().startTime
        val ssStart = intervalsFromSS.first().start
        assertEquals(ssStart, modelStart)

        /* Check that the last intervals end at the same timestamp too. */
        val modelEnd = intervalsFromRender.last().endTime
        val ssEnd = intervalsFromSS.last().end
        assertEquals(ssEnd, modelEnd)
    }

    private fun verifySameIntervals(ssIntervals: List<StateInterval>,
                                    renderIntervals: List<TimeGraphStateInterval>) {
        assertEquals(ssIntervals.size, renderIntervals.size)

        for (i in 1 until ssIntervals.size) {
            val ssInterval = ssIntervals[i]
            val renderInterval = renderIntervals[i]

            assertEquals(ssInterval.start, renderInterval.startEvent.timestamp)
            assertEquals(ssInterval.end, renderInterval.endEvent.timestamp)

            val stateValue = ssInterval.stateValue
            val stateName = ThreadsModelStateProvider.stateValueToStateDef(stateValue).name
            assertEquals(stateName, renderInterval.stateName)
        }
    }
}