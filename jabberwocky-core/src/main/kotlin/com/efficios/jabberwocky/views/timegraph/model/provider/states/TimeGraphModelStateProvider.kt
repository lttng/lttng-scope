/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider.states;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.StateDefinition;
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateRender;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty

import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provider for timegraph state intervals.
 *
 * It can be used stand-alone (ie, for testing) but usually would be part of a
 * {@link ITimeGraphModelProvider}.
 *
 * @author Alexandre Montplaisir
 */
abstract class TimeGraphModelStateProvider(val stateDefinitions: List<StateDefinition>) {

    /**
     * Property representing the trace this arrow provider fetches its data for.
     */
    private val traceProjectProperty: ObjectProperty<TraceProject<*, *>?> = SimpleObjectProperty(null)
    fun traceProjectProperty() = traceProjectProperty
    var traceProject
        get() = traceProjectProperty.get()
        set(value) = traceProjectProperty.set(value)

    /**
     * Get a state render for a single tree element
     *
     * @param treeElement
     *            The tree element to target
     * @param timeRange
     *            The time range of the query
     * @param resolution
     *            The resolution, in timestamp units, of the query. For example,
     *            a resolution of 1000 means the provider does not need to
     *            return states that don't cross at least 1 1000-units borders.
     * @param task
     *            An optional task parameter, which can be checked for
     *            cancellation to stop processing at any point and return.
     * @return The corresponding state render for this tree element and settings
     */
    abstract fun getStateRender(treeElement: TimeGraphTreeElement,
                                timeRange: TimeRange,
                                resolution: Long,
                                task: FutureTask<*>?): TimeGraphStateRender

    open fun getStateRenders(treeElements: Set<TimeGraphTreeElement>,
                        timeRange: TimeRange,
                        resolution: Long,
                        task: FutureTask<*>?): Map<TimeGraphTreeElement, TimeGraphStateRender> {
        return treeElements.associate { Pair(it, getStateRender(it, timeRange, resolution, task)) }
    }

    /**
     * Helper method to fetch all the state renders for all tree elements of a
     * *tree* render.
     *
     * Default implementation simply calls {@link #getStateRender} on each tree
     * element sequentially, but more advanced providers could override it if
     * they can provide a faster mechanism.
     *
     * @param treeRender
     *            The tree render for which to prepare all the state renders
     * @param timeRange
     *            The time range of the query
     * @param resolution
     *            The resolution of the query, see {@link #getStateRender}.
     * @param task
     *            The optional task parameter which can be used for
     *            cancellation.
     * @return The corresponding state renders
     */
    open fun getAllStateRenders(treeRender: TimeGraphTreeRender,
                           timeRange: TimeRange,
                           resolution: Long,
                           task: FutureTask<*>?): List<TimeGraphStateRender> {
        return treeRender.allTreeElements.map { getStateRender(it, timeRange, resolution, task) }
    }

}
