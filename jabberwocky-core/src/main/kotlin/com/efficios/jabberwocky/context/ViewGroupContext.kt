/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.context;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.trace.Trace
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty;

/**
 * A common context for a group of views. Information is stored as properties,
 * and views can add listeners to get notified of value changes.
 *
 * Architecturally, the context of a given component should be constant (final)
 * for the lifetime of the component. If you wish to use a new context, then new
 * component instances should be created.
 *
 * The current project is not implemented using a standard JavaFX property, but instead
 * by implementing a [ProjectChangeListener] and registering it via [registerProjectChangeListener].
 *
 * If you attach change listeners to the time range properties, it is *very* important
 * to check the value of the [listenerFreeze] property within the listener. If it is true,
 * no action should be taken.
 * This is because some underlying operation - like a project-switching - is ongoing, and
 * the new values may not make sense on the current/previous project.
 */
class ViewGroupContext {

    companion object {
        /** Value representing uninitialized timestamps */
        @JvmField
        val UNINITIALIZED_RANGE = TimeRange.of(0, 0);
    }

    /**
     * Switching project is slightly more involved than just changing the value of a ObjectProperty.
     * Implementing this interface (including the 'flush()' method) will guarantee that only valid
     * values will ever be seen by listening components.
     */
    abstract class ProjectChangeListener(val owner: Any) {

        /**
         * Flush all currently queued operations that might work with the current project.
         *
         * For example, if queries are done in separate threads, implementation of the flush()
         * method should stop all currently queued threads.
         *
         * If a component does not use separate threads then this can be a no-op.
         */
        open fun flush() {
            /* Default implementation is to do nothing. Sub-classes can override. */
        }

        /**
         * Action to take on a project switching operation.
         *
         * The values of currentSelection and currentVisibleRange will be valid
         * once this is called.
         */
        abstract fun newProjectCb(newProject: TraceProject<*, *>?)
    }

    private val projectChangeListeners = mutableSetOf<ProjectChangeListener>()

    @Synchronized
    fun registerProjectChangeListener(listener: ProjectChangeListener): TraceProject<*, *>? {
        projectChangeListeners.add(listener)
        return traceProject
    }

    @Synchronized
    fun deregisterProjectChangeListener(listener: ProjectChangeListener) {
        projectChangeListeners.remove(listener)
    }

    @Volatile
    var listenerFreeze = false
        private set

    var traceProject: TraceProject<*, *>? = null
        private set

    @Synchronized
    fun switchProject(newProject: TraceProject<*, *>?) {
        val prevProject = traceProject
        if (prevProject != null && newProject != null) {
            /* Don't switch directly from one "real" project to another, go through a null project in-between. */
            switchProjectImpl(null)
            switchProjectImpl(newProject)
        } else {
            /* If we're going to or from a null-project, we can do it directly. */
            switchProjectImpl(newProject)
        }
    }

    private fun switchProjectImpl(newProject: TraceProject<*, *>?) {
        listenerFreeze = true
        projectChangeListeners.forEach { it.flush() }

        traceProject = newProject

        if (newProject == null) {
            visibleTimeRange = UNINITIALIZED_RANGE
            selectionTimeRange = UNINITIALIZED_RANGE
        } else {
            /* Initial visible range should be the whole project. Initial selection should be the first timestamp. */
            val start = newProject.startTime
            val end = newProject.endTime
            visibleTimeRange = TimeRange.of(start, end)
            selectionTimeRange = TimeRange.of(start, start)
        }

        projectChangeListeners.forEach { it.newProjectCb(newProject) }
        listenerFreeze = false
    }

    /**
     * Current visible time range in the context
     *
     * Do not forget to check the value of [listenerFreeze] inside any ChangeListener attached
     * to this property. If it is true, then no action should be taken using the value, because
     * it might not be consistent with the current project.
     * Once [listenerFreeze] becomes false again, then it is safe to use the values.
     */
    private val visibleTimeRangeProperty: ObjectProperty<TimeRange> = SimpleObjectProperty(UNINITIALIZED_RANGE)
    fun visibleTimeRangeProperty() = visibleTimeRangeProperty
    var visibleTimeRange: TimeRange
        get() = visibleTimeRangeProperty.get()
        set(value) = visibleTimeRangeProperty.set(value)

    /**
     * Current time range selection
     *
     * Do not forget to check the value of [listenerFreeze] inside any ChangeListener attached
     * to this property. If it is true, then no action should be taken using the value, because
     * it might not be consistent with the current project.
     * Once [listenerFreeze] becomes false again, then it is safe to use the values.
     */
    private val selectionTimeRangeProperty: ObjectProperty<TimeRange> = SimpleObjectProperty(UNINITIALIZED_RANGE)
    fun selectionTimeRangeProperty() = selectionTimeRangeProperty
    var selectionTimeRange: TimeRange
        get() = selectionTimeRangeProperty.get()
        set(value) = selectionTimeRangeProperty.set(value)


    /**
     * Utility method to get the full range of the current active trace project.
     *
     * @return The full range of the current trace project, or
     *         {@link #UNINITIALIZED_RANGE} if there is no active project.
     */
    fun getCurrentProjectFullRange(): TimeRange {
        return traceProject?.fullRange ?: UNINITIALIZED_RANGE
    }

    /**
     * Move the visible range to be centered on a target timestamp.
     *
     * The visible range span (or duration) will remain the same, we will simply "slide" the window.
     * This means it might be clamped to the end or start time of the project.
     *
     * Note this will not apply a selection on the target timetamp. If this is desired, it should
     * be done separately.
     */
    fun centerVisibleRangeOn(timestamp: Long) {
        val project = traceProject ?: return
        if (timestamp !in project.startTime..project.endTime) throw IllegalArgumentException("Target timestamp outside of project range")

        /* We will try our best to keep the current visible range span the same. */
        val span = visibleTimeRange.duration
        val halfSpan = span / 2
        visibleTimeRange = when {
            /* Clamp to start time */
            timestamp - halfSpan < project.startTime -> TimeRange.of(project.startTime, project.startTime + span)
            /* Clamp to end time */
            timestamp + halfSpan > project.endTime -> TimeRange.of(project.endTime - span, project.endTime)
            /* Simply center on the target timestamp, it should fit. */
            else -> TimeRange.of(timestamp - halfSpan, timestamp + halfSpan)
        }
    }
}
