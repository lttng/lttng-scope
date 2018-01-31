/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.layer.drawnevents

import com.efficios.jabberwocky.common.ConfigOption
import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.timegraph.model.provider.drawnevents.TimeGraphDrawnEventProvider
import com.efficios.jabberwocky.views.timegraph.model.render.TimeGraphEvent
import com.efficios.jabberwocky.views.timegraph.model.render.drawnevents.TimeGraphDrawnEvent
import com.efficios.jabberwocky.views.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender
import com.efficios.jabberwocky.views.timegraph.model.render.drawnevents.TimeGraphDrawnEventSeries
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender
import org.lttng.scope.project.filter.EventFilterDefinition
import org.lttng.scope.views.context.ViewGroupContextManager
import java.util.concurrent.FutureTask

/** Maximum number of matching events */
private const val MAX_EVENTS = 2000

/**
 * Provider of drawn event series based on a project event-filter.
 */
class PredicateDrawnEventProvider(private val eventFilter: EventFilterDefinition) : TimeGraphDrawnEventProvider(eventFilter.createSeries()) {

    private val projectChangeListener = object : ViewGroupContext.ProjectChangeListener {
        override fun newProjectCb(newProject: TraceProject<*, *>?) {
            /* Effectively just a bind() */
            traceProject = newProject
        }
    }

    init {
        /* Just use whatever trace is currently active */
        val currentProject = ViewGroupContextManager.getCurrent().registerProjectChangeListener(projectChangeListener)
        traceProject = currentProject

        /* This provider will be active whenever the project filter is enabled */
        enabledProperty().bind(eventFilter.enabledProperty())
    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        ViewGroupContextManager.getCurrent().deregisterProjectChangeListener(projectChangeListener)
    }

    override fun getEventRender(treeRender: TimeGraphTreeRender, timeRange: TimeRange, task: FutureTask<*>?): TimeGraphDrawnEventRender {
        val project = traceProject ?: return TimeGraphDrawnEventRender(timeRange, emptyList())

        // TODO We could keep the iterator open for the lifetime of the provider, so that
        // the same iterator is reused from one render to another.

        val matchingEvents = project.iterator().use {
            it.seek(timeRange.startTime)
            it.asSequence()
                    .takeWhile { it.timestamp <= timeRange.endTime }
                    .filter(eventFilter.predicate)
                    .take(MAX_EVENTS)
                    .toList()
        }

        val drawnEvents = matchingEvents
                /* trace event -> TimeGraphEvent */
                .mapNotNull { traceEvent ->
                    treeRender.allTreeElements
                            .find { it.eventMatching?.test(traceEvent) == true }
                            ?.let { TimeGraphEvent(traceEvent.timestamp, it) }
                }
                /* TimeGraphEvent -> TimeGraphDrawnEvent */
                .map { TimeGraphDrawnEvent(it, drawnEventSeries, null) }

        return TimeGraphDrawnEventRender(timeRange, drawnEvents)
    }

}

private fun EventFilterDefinition.createSeries() =
        TimeGraphDrawnEventSeries(this.name,
                ConfigOption(this.color),
                ConfigOption(this.symbol))
