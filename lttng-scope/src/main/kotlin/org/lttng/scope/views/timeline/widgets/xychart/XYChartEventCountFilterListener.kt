/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.xychart

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProvider
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartSeriesProvider
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender
import com.efficios.jabberwocky.views.xychart.model.render.XYChartSeries
import org.lttng.scope.project.ProjectFilters
import org.lttng.scope.project.ProjectManager
import org.lttng.scope.project.filter.EventFilterDefinition
import java.util.*
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicLong

private const val RESULTS_LIMIT = 20_000

/**
 * Filter listener that will listen to project filter creation/removal, and will create/remove
 * corresponding series for the "event count" xy charts.
 */
class XYChartEventCountFilterListener(private val viewContext: ViewGroupContext,
                                      private val modelProvider: XYChartModelProvider) : ProjectFilters.FilterListener {

    private val createdSeriesProviders = mutableMapOf<EventFilterDefinition, FilterSeriesProvider>()

    private val projectChangeListener = object : ViewGroupContext.ProjectChangeListener {
        override fun newProjectCb(newProject: TraceProject<*, *>?) {
            /* On project change, clear the current providers. */
            createdSeriesProviders.values.forEach { modelProvider.removeSeries(it) }
            createdSeriesProviders.clear()

            /* Re-register to the new project, if there is one. */
            newProject?.let { ProjectManager.getProjectState(it).filters.registerFilterListener(this@XYChartEventCountFilterListener) }
        }
    }

    init {
        /* Initialize with current project, and attach listener for project changes. */
        viewContext.registerProjectChangeListener(projectChangeListener)
                /* Register to initial project, if there is one. */
                ?.let { ProjectManager.getProjectState(it).filters.registerFilterListener(this@XYChartEventCountFilterListener) }
    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        viewContext.deregisterProjectChangeListener(projectChangeListener)
    }

    override fun filterCreated(filter: EventFilterDefinition) {
        if (createdSeriesProviders.containsKey(filter)) throw IllegalArgumentException("Duplicate filter registered: $filter")

        FilterSeriesProvider(filter).let {
            createdSeriesProviders.put(filter, it)
            modelProvider.registerSeries(it)
        }
    }

    override fun filterRemoved(filter: EventFilterDefinition) {
        createdSeriesProviders.remove(filter)?.let { modelProvider.removeSeries(it) }
    }


    private inner class FilterSeriesProvider(private val filter: EventFilterDefinition) : XYChartSeriesProvider(filter.createSeries()) {

        override fun generateSeriesRender(range: TimeRange, resolution: Long, task: FutureTask<*>?): XYChartRender {
            // TODO Currently reading all trace events every query.
            // Instead we could build a state system of events matching the filter and re-use that.
            val proj = viewContext.traceProject ?: return XYChartRender.EMPTY_RENDER

            /*
             * Map<timestamp, matching event count>
             * We use an AtomicLong just because we want a mutable Long...
             */
            val eventsMap = (range.startTime..range.endTime step resolution)
                    .associateByTo(TreeMap(), { it }, { AtomicLong(0) })

            /* Aggregate the matching events into buckets each representing a data point. */
            proj.iterator().use {
                it.seek(range.startTime)
                it.asSequence()
                        .takeWhile { it.timestamp <= range.endTime }
                        .filter(filter.predicate)
                        .take(RESULTS_LIMIT)
                        .forEach { eventsMap.floorEntry(it.timestamp)?.let { it.value.incrementAndGet() } }
            }

            val datapoints = eventsMap
                    .map { XYChartRender.DataPoint(it.key, it.value.get()) }
                    .toList()

            return XYChartRender(series, range, datapoints)
        }

    }
}

private fun EventFilterDefinition.createSeries() = XYChartSeries(name, color, XYChartSeries.LineStyle.FULL)
