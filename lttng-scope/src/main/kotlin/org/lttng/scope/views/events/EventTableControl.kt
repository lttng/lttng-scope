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
import javafx.beans.InvalidationListener
import org.lttng.scope.application.ScopeOptions
import org.lttng.scope.application.task.ScopeTask
import org.lttng.scope.common.LatestTaskExecutor
import java.util.*
import java.util.logging.Logger

class EventTableControl(internal val viewContext: ViewGroupContext) {

    companion object {
        private val LOGGER = Logger.getLogger(EventTableControl::class.java.name)

        /** How many events to fetch *in each direction*, limited by the start/end of the project. */
        private const val FETCH_SIZE = 25_000
    }

    private val projectChangeListener = object : ViewGroupContext.ProjectChangeListener {
        override fun flush() {
            /* Stop the redraw task and wait for it to finish */
            val bubble = object : ScopeTask(null) {
                override fun execute() {
                }
            }
            taskExecutor.schedule(bubble)
            bubble.get() /* Wait for it be scheduled and finish execution. */
        }

        override fun newProjectCb(newProject: TraceProject<*, *>?) {
            if (newProject == null) {
                clearView()
            } else {
                initializeForProject(newProject)
            }
        }
    }

    private val timestampFormatChangeListener = InvalidationListener {
        table.refresh()
    }

    val table = EventTable(this)
    private val taskExecutor = LatestTaskExecutor()

    private var currentBackwardsEvents: List<TraceEvent>? = null
    private var currentForwardsEvents: List<TraceEvent>? = null

    init {
        viewContext.registerProjectChangeListener(projectChangeListener)

        viewContext.selectionTimeRangeProperty().addListener { _, _, newRange ->
            if (viewContext.listenerFreeze) return@addListener
            viewContext.traceProject?.let { recenterOn(it, newRange.startTime) }
        }

        ScopeOptions.timestampFormatProperty().addListener(timestampFormatChangeListener)
        ScopeOptions.timestampTimeZoneProperty().addListener(timestampFormatChangeListener)
    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        viewContext.deregisterProjectChangeListener(projectChangeListener)
        ScopeOptions.timestampFormatProperty().removeListener(timestampFormatChangeListener)
        ScopeOptions.timestampTimeZoneProperty().removeListener(timestampFormatChangeListener)
    }

    @Synchronized
    fun scrollToBeginning() {
        val project = viewContext.traceProject ?: return
        initializeForProject(project)
    }

    @Synchronized
    fun pageUp() {
        val project = viewContext.traceProject ?: return
        /*
         * The "currentBackwardsEvents" will become the new "forwardsEvents",
         * and we will fetch the previous "page" as the new backwardsEvents.
         */
        val currentEvents = currentBackwardsEvents ?: return
        if (currentEvents.isEmpty()) return
        val ts = currentEvents.first().timestamp
        val previousEvents = project.iterator().use {
            it.seek(ts)
            fetchPreviousEvents(it, FETCH_SIZE)
        }

        val fullEventList = listOf(previousEvents + currentEvents).flatten()

        currentBackwardsEvents = previousEvents
        currentForwardsEvents = currentEvents

        table.displayEvents(fullEventList)
        table.selectIndex(previousEvents.size)
    }

    @Synchronized
    fun pageDown() {
        val project = viewContext.traceProject ?: return
        /*
         * The "currentForwardsEvents" will become the new "backwardsEvents",
         * and we will fetch the next "page" as the new forwardsEvents.
         */
        val currentEvents = currentForwardsEvents ?: return
        if (currentEvents.isEmpty()) return
        val ts = currentEvents.last().timestamp
        val nextEvents = project.iterator().use {
            it.seek(ts)
            /* Consume the last of "currentEvents", we already have it. */
            it.next()
            /* then read the next page */
            it.asSequence().take(FETCH_SIZE).toList()
        }

        val fullEventList = listOf(currentEvents + nextEvents).flatten()

        currentBackwardsEvents = currentEvents
        currentForwardsEvents = nextEvents

        table.displayEvents(fullEventList)
        table.selectIndex(currentEvents.size)
    }

    @Synchronized
    fun scrollToEnd() {
        val project = viewContext.traceProject ?: return
        val events = project.iterator().use {
            it.seek(Long.MAX_VALUE)
            fetchPreviousEvents(it, FETCH_SIZE)
        }
        currentBackwardsEvents = events
        currentForwardsEvents = null

        table.displayEvents(events)
        table.scrollToBottom()
    }

    private fun clearView() {
        table.clearTable()
    }

    private fun initializeForProject(project: TraceProject<*, *>) {
        val firstEvents = project.iterator().use { it.asSequence().take(FETCH_SIZE).toList() }
        currentBackwardsEvents = null
        currentForwardsEvents = firstEvents

        table.displayEvents(firstEvents)
        table.scrollToTop()
    }

    @Synchronized
    private fun recenterOn(project: TraceProject<*, *>, timestamp: Long) {
        val task = object : ScopeTask("Fetching Event Table Contents") {
            override fun execute() {
                // TODO Implement TraceProjectIterator.copy(), use it here instead of seeking twice
                val forwardsEvents = project.iterator().use {
                    it.seek(timestamp)
                    it.asSequence().take(FETCH_SIZE).toList()
                }

                val backwardsEvents = project.iterator().use {
                    it.seek(timestamp)
                    fetchPreviousEvents(it, FETCH_SIZE)
                }

                val eventIndex = backwardsEvents.size
                val eventsList = listOf(backwardsEvents + forwardsEvents).flatten()

                LOGGER.finer { "Backwards events: ${logEventsToString(backwardsEvents)}" }
                LOGGER.finer { "Forwards events: ${logEventsToString(forwardsEvents)}" }

                if (isCancelled) return

                currentBackwardsEvents = backwardsEvents
                currentForwardsEvents = forwardsEvents

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