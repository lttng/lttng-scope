/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.states;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.StateDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.efficios.jabberwocky.common.TimeRange;

import javafx.beans.property.ObjectProperty;

/**
 * Provider for timegraph state intervals.
 *
 * It can be used stand-alone (ie, for testing) but usually would be part of a
 * {@link org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProvider}.
 *
 * @author Alexandre Montplaisir
 */
public interface ITimeGraphModelStateProvider {

    /**
     * Property representing the trace this arrow provider fetches its data for.
     *
     * @return The trace property
     */
    ObjectProperty<@Nullable ITmfTrace> traceProperty();

    /**
     * Get an aggregated list of {@link StateDefinition} used in this provider.
     *
     * @return The state definitions used in this provider
     */
    List<StateDefinition> getStateDefinitions();

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
    TimeGraphStateRender getStateRender(TimeGraphTreeElement treeElement,
            TimeRange timeRange, long resolution, @Nullable FutureTask<?> task);

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
    default List<TimeGraphStateRender> getStateRenders(TimeGraphTreeRender treeRender, TimeRange timeRange, long resolution, @Nullable FutureTask<?> task) {
        return treeRender.getAllTreeElements().stream()
                .map(treeElem -> getStateRender(treeElem, timeRange, resolution, task))
                .collect(Collectors.toList());
    }

}
