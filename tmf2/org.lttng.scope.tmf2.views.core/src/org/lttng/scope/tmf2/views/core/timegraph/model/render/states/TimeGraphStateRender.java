/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.render.states;

import java.util.List;

import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;

import com.google.common.collect.ImmutableList;

/**
 * "Segment" of a time graph, representing the states of a single tree element
 * for a given time range.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphStateRender {

    private final TimeRange fTimeRange;
    private final TimeGraphTreeElement fTreeElement;
    private final List<TimeGraphStateInterval> fStateIntervals;

    /**
     * Constructor
     *
     * @param timeRange
     *            The time range of this state render. This is for informative
     *            use mostly, and usually matches the time range that was
     *            requested for the query that generated this render.
     * @param treeElement
     *            This render contains state intervals of this single tree
     *            element
     * @param stateIntervals
     *            The state intervals that are part of this render. Their range
     *            should normally match the 'timeRange' parameter.
     */
    public TimeGraphStateRender(TimeRange timeRange,
            TimeGraphTreeElement treeElement,
            List<TimeGraphStateInterval> stateIntervals) {

        fTimeRange = timeRange;
        fTreeElement = treeElement;
        fStateIntervals = ImmutableList.copyOf(stateIntervals);
    }

    /**
     * Get the time range of this interval.
     *
     * @return The time range
     */
    public TimeRange getTimeRange() {
        return fTimeRange;
    }

    /**
     * Get the tree element to which the intervals of this render belongs.
     *
     * @return The tree element
     */
    public TimeGraphTreeElement getTreeElement() {
        return fTreeElement;
    }

    /**
     * Get the state intervals that are part of this state render
     *
     * @return The state intervals
     */
    public List<TimeGraphStateInterval> getStateIntervals() {
        return fStateIntervals;
    }

}
