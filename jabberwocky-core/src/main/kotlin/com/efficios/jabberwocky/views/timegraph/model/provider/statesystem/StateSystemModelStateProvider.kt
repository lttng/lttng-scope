/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider.statesystem

import ca.polymtl.dorsal.libdelorean.IStateSystemQuarkResolver
import ca.polymtl.dorsal.libdelorean.IStateSystemReader
import ca.polymtl.dorsal.libdelorean.interval.StateInterval
import ca.polymtl.dorsal.libdelorean.iterator2D
import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis
import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.common.intersection
import com.efficios.jabberwocky.views.timegraph.model.provider.states.TimeGraphModelStateProvider
import com.efficios.jabberwocky.views.timegraph.model.render.StateDefinition
import com.efficios.jabberwocky.views.timegraph.model.render.states.MultiStateInterval
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateInterval
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateRender
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender
import java.util.*
import java.util.concurrent.FutureTask

/**
 * Basic implementation of a {@link TimeGraphModelStateProvider} backed by a state
 * system.
 *
 * @author Alexandre Montplaisir
 */
abstract class StateSystemModelStateProvider(stateDefinitions: List<StateDefinition>,
                                             stateSystemAnalysis: StateSystemAnalysis) : TimeGraphModelStateProvider(stateDefinitions) {

    /**
     * This state system here is not necessarily the same as the one in the
     * {@link StateSystemModelProvider}!
     */
    @Transient
    private var stateSystem: IStateSystemReader? = null

    init {
        /*
         * Change listener which will take care of keeping the target state
         * system up to date.
         */
        traceProjectProperty().addListener { _, _, newValue ->
            val project = newValue
            stateSystem = if (project != null
                    && stateSystemAnalysis.appliesTo(project)
                    && stateSystemAnalysis.canExecute(project)) {
                // TODO Cache this?
                stateSystemAnalysis.execute(project, null, null)
            } else {
                null
            }
        }

    }

    /**
     * Supply a list of additional quarks this provider would need to query to
     * generate a complete model interval from the given state interval.
     *
     * The quarks included here will be queried, and the results will be passed
     * back in {@link #createInterval}.
     */
    protected abstract fun supplyExtraQuarks(ss: IStateSystemQuarkResolver,
                                             ts: Long,
                                             stateInterval: StateInterval): Set<Int>

    /**
     * Define how this state provider generates model intervals.
     *
     * @param ssQueryResult
     *            Results of the state system query containing the requested
     *            extra data.
     * @param treeElem
     *            The timegraph tree element (FIXME Required because of the state
     *            interval's constructor, otherwise the subclasses should only need
     *            the quark)
     * @param interval
     *            The source interval
     * @return The timegraph model interval object, you can use
     *         {@link BasicTimeGraphStateInterval} for a simple implementation.
     */
    protected abstract fun createInterval(ss: IStateSystemQuarkResolver,
                                          ssQueryResult: Map<Int, StateInterval>,
                                          treeElem: StateSystemTimeGraphTreeElement,
                                          interval: StateInterval): TimeGraphStateInterval

    // ------------------------------------------------------------------------
    // Render generation methods
    // ------------------------------------------------------------------------

    override fun getStateRender(treeElement: TimeGraphTreeElement,
                                timeRange: TimeRange,
                                resolution: Long,
                                task: FutureTask<*>?): TimeGraphStateRender {

        /* "Title" entries should be ignored */
        if (treeElement !is StateSystemTimeGraphTreeElement) {
            return TimeGraphStateRender.EMPTY_RENDER
        }

        val renders = getStateRenders(setOf(treeElement), timeRange, resolution, task)
        if (renders.isEmpty()) return TimeGraphStateRender.EMPTY_RENDER
        return renders.values.single()
    }

    override fun getStateRenders(treeElements: Set<TimeGraphTreeElement>,
                                 timeRange: TimeRange,
                                 resolution: Long,
                                 task: FutureTask<*>?): Map<TimeGraphTreeElement, TimeGraphStateRender> {

        val ss = stateSystem
        /* Early-exit if there is no state system (project) currently set. */
        if (ss == null || (task != null && task.isCancelled)) return Collections.emptyMap()

        /* Clamp the requested time range to the known valid time range for the state database. */
        val queryTimeRange = timeRange.intersection(ss.startTime, ss.currentEndTime) ?: return Collections.emptyMap()

        val intervalsPerElement = treeElements.associateBy({ it }, { mutableListOf<TimeGraphStateInterval>() })

        val ssTreeElements = treeElements
                .filter { it is StateSystemTimeGraphTreeElement }
                .map { it as StateSystemTimeGraphTreeElement }
        val quarksToTreeElementMap = ssTreeElements.associateBy { it.sourceQuark }

        // TODO Check the task parameter during the iteration (for loop?)

        /* Query the intervals from the state system */
        val quarks = ssTreeElements.map { it.sourceQuark }.toSet()
        ss.iterator2D(queryTimeRange.startTime, queryTimeRange.endTime, resolution, quarks).asSequence()
                .forEach { iterationStep ->
                    val ts = iterationStep.ts
                    val queryResults = iterationStep.queryResults

                    /*
                     * Compute all the extra data the model implementation will
                     * need to fetch from the state system.
                     */
                    val requestedQuarks = queryResults.values
                            .flatMap { supplyExtraQuarks(ss, ts, it) }
                            .distinct()
                            .toSet()

                    /* Query in one go all the requested extra data. */
                    val extraData = ss.queryStates(ts, requestedQuarks)

                    /*
                     * Re-call the model implementation to generate the
                     * corresponding model intervals, supplying the extra data.
                     */
                    queryResults.forEach {
                        val quark = it.key
                        val stateInterval = it.value
                        val treeElement = quarksToTreeElementMap[quark]!!
                        val modelInterval = createInterval(ss, extraData, treeElement, stateInterval)
                        /* Insert into the correct list among the ones we created earlier */
                        intervalsPerElement[treeElement]!!.add(modelInterval)
                    }
                }

        /*
         * Manually add the entries for the last pixel [endTime - resolution, endTime].
         * The iterator doesn't return them.
         */
        val lastResolutionPt = queryTimeRange.startTime + (queryTimeRange.duration / resolution) * resolution
        val extraData = ss.queryFullState(queryTimeRange.endTime).associateBy { it.attribute }
        ss.queryStates(queryTimeRange.endTime, ssTreeElements.map { it.sourceQuark }.toSet())
                .forEach { quark, interval ->
                    val treeElem = quarksToTreeElementMap[quark]!!
                    val targetIntervalList = intervalsPerElement[treeElem]!!
                    if (interval.intersects(lastResolutionPt)
                            && interval.end != targetIntervalList.last().endTime) {

                        val modelInterval = createInterval(ss, extraData, treeElem, interval)
                        targetIntervalList.add(modelInterval)
                    }
                }

        /*
         * 'intervalsPerElement' now contains all the real state intervals that
         * will be part of our model. Poly-filla the holes between those
         * intervals with multi-states before returning.
         */
        return intervalsPerElement
                .mapValues { entry -> fillWithMultiStates(queryTimeRange, entry.key, entry.value) }
                .mapValues { entry -> TimeGraphStateRender(queryTimeRange, entry.key, entry.value) }
    }

    override fun getAllStateRenders(treeRender: TimeGraphTreeRender,
                                    timeRange: TimeRange,
                                    resolution: Long,
                                    task: FutureTask<*>?): List<TimeGraphStateRender> {

        /* Ensure the returned list has the same order as the .allTreeElements */
        val rendersMap = getStateRenders(treeRender.allTreeElements.toSet(), timeRange, resolution, task)
        return treeRender.allTreeElements.map { rendersMap[it]!! }
    }

    private fun fillWithMultiStates(timeRange: TimeRange,
                                    treeElem: TimeGraphTreeElement,
                                    modelIntervals: List<TimeGraphStateInterval>): List<TimeGraphStateInterval> {

        if (modelIntervals.size < 2) {
            return modelIntervals
        }

        val filledIntervals = LinkedList<TimeGraphStateInterval>()

        /*
         * Add the first real interval. There might be a multi-state at the
         * beginning.
         */
        val firstRealIntervalStartTime = modelIntervals[0].startTime
        if (modelIntervals[0].startTime > timeRange.startTime) {
            filledIntervals.add(MultiStateInterval(timeRange.startTime, firstRealIntervalStartTime - 1, treeElem))
        }
        filledIntervals.add(modelIntervals[0])

        for (i in 1 until modelIntervals.size) {
            val interval1 = modelIntervals[i - 1]
            val interval2 = modelIntervals[i]
            val bound1 = interval1.endTime
            val bound2 = interval2.startTime

            /* (we've already inserted 'interval1' on the previous loop.) */
            if (bound1 + 1 != bound2) {
                val multiStateInterval = MultiStateInterval(bound1 + 1, bound2 - 1, treeElem)
                filledIntervals.add(multiStateInterval)
            }
            filledIntervals.add(interval2)
        }

        /* Add a multi-state at the end too, if needed */
        val lastRealIntervalEndTime = modelIntervals.last().endTime
        if (lastRealIntervalEndTime < timeRange.endTime) {
            filledIntervals.add(MultiStateInterval(lastRealIntervalEndTime + 1, timeRange.endTime, treeElem))
        }

        return filledIntervals
    }

}
