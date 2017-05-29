/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.config.ConfigOption;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.states.TimeGraphModelStateProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.LineThickness;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.BasicTimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;

import com.efficios.jabberwocky.common.TimeRange;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

class StubModelStateProvider extends TimeGraphModelStateProvider {

    /**
     * The duration of each state is equal to its tree element index, multiplied
     * by this factor.
     */
    public static final long DURATION_FACTOR = 10;

    public StubModelStateProvider() {
        super(ImmutableList.of());
    }

    @Override
    public TimeGraphStateRender getStateRender(TimeGraphTreeElement treeElement,
            TimeRange timeRange, long resolution, @Nullable FutureTask<?> task) {

        if (treeElement == StubModelProvider.ROOT_ELEMENT) {
            return TimeGraphStateRender.EMPTY_RENDER;
        }

        int entryIndex = Integer.valueOf(treeElement.getName().substring(StubModelProvider.ENTRY_NAME_PREFIX.length()));
        long stateLength = entryIndex * DURATION_FACTOR;

        List<TimeGraphStateInterval> intervals = LongStream.iterate(timeRange.getStartTime(), i -> i + stateLength)
                .limit((timeRange.getDuration() / stateLength) + 1)
                .mapToObj(startTime -> {
                    long endTime = startTime + stateLength - 1;
                    String name = getNextStateName();
                    ConfigOption<ColorDefinition> color = getnextStateColor();
                    return new BasicTimeGraphStateInterval(startTime, endTime, treeElement, name, name, color, LINE_THICKNESS, Collections.emptyMap());
                })
                .collect(Collectors.toList());

        return new TimeGraphStateRender(timeRange, treeElement, intervals);
    }

    private static final ConfigOption<LineThickness> LINE_THICKNESS = new ConfigOption<>(LineThickness.NORMAL);
    private static final Iterator<String> STATE_NAMES = Iterators.cycle("State 1", "State 2");
    private static final Iterator<ConfigOption<ColorDefinition>> STATE_COLORS = Iterators.cycle(
            new ConfigOption<>(new ColorDefinition(128, 0, 0)),
            new ConfigOption<>(new ColorDefinition(0, 0, 128)));

    private static synchronized String getNextStateName() {
        return STATE_NAMES.next();
    }

    private static synchronized ConfigOption<ColorDefinition> getnextStateColor() {
        return STATE_COLORS.next();
    }

}
