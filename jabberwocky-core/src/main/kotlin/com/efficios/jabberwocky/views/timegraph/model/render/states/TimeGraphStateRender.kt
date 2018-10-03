/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.render.states

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement

/**
 * "Segment" of a time graph, representing the states of a single tree element
 * for a given time range.
 *
 * @author Alexandre Montplaisir
 */
data class TimeGraphStateRender(val timeRange: TimeRange,
                                val treeElement: TimeGraphTreeElement,
                                val stateIntervals: List<TimeGraphStateInterval>) {

    companion object {
        /** Non-null reference to a dummy/empty render */
        @JvmField
        val EMPTY_RENDER = TimeGraphStateRender(TimeRange.of(0, 0), TimeGraphTreeElement.DUMMY_ELEMENT, emptyList());
    }

}