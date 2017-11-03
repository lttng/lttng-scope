/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.events

import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.project.TraceProjectIterator
import com.efficios.jabberwocky.trace.event.TraceEvent
import javafx.concurrent.Task
import org.lttng.scope.utils.LatestTaskExecutor
import org.lttng.scope.utils.logger
import java.util.*

class EventTableControl(private val viewContext: ViewGroupContext) {

    companion object {
        private val LOGGER by logger()

        /** How many events to fetch *in each direction*, limited by the start/end of the project. */
        private const val FETCH_SIZE = 25_000
    }

    val table = EventTable()
    private val taskExecutor = LatestTaskExecutor()

    init {
        viewContext.currentTraceProjectProperty().addListener { _, _, newProject ->
            if (newProject == null) {
                clearView()
            } else {
                initializeForProject(newProject)
            }
            viewContext.currentSelectionTimeRangeProperty().addListener { _, _, newRange ->
                val project = viewContext.currentTraceProject
                if (project != null) recenterOn(project, newRange.startTime)
            }
        }
    }

    private fun clearView() {
        table.clearTable()
    }

    private fun initializeForProject(project: TraceProject<*, *>) {
        val firstEvents = project.iterator().use { it.asSequence().take(FETCH_SIZE).toList() }
        table.displayEvents(firstEvents)
        table.scrollToTop()
    }

    private fun recenterOn(project: TraceProject<*, *>, timestamp: Long) {
        val task = object : Task<Unit>() {
            override fun call() {
                // TODO Implement TraceProjectIterator.copy(), use it here instead of seeking twice
                val forwardEvents = project.iterator().use {
                    it.seek(timestamp)
                    it.asSequence().take(FETCH_SIZE).toList()
                }

                val backwardsEvents = project.iterator().use {
                    it.seek(timestamp)
                    fetchPreviousEvents(it, FETCH_SIZE)
                }

                val eventIndex = backwardsEvents.size
                val eventsList = listOf(backwardsEvents + forwardEvents).flatten()

                LOGGER.finer { "Backwards events: ${logEventsToString(backwardsEvents)}" }
                LOGGER.finer { "Forwards events: ${logEventsToString(forwardEvents)}" }

                table.displayEvents(eventsList)
                table.selectIndex(eventIndex)
            }
        }
        taskExecutor.schedule(task)
    }

    private fun <E : TraceEvent> fetchPreviousEvents(iterator: TraceProjectIterator<E>, limit: Int): List<E> {
        if (limit < 0) throw IllegalArgumentException()
        var left = limit
        val events: MutableList<E> = LinkedList()
        while (iterator.hasPrevious() && left > 0) {
            events.add(iterator.previous())
            left--
        }
        return events.asReversed()
    }

    private fun logEventsToString(events: List<TraceEvent>): String {
        return if (events.isEmpty()) {
            "none"
        } else {
            "${events.first().timestamp} to ${events.last().timestamp}, ${events.size} total"
        }
    }
}