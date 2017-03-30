/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.TimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tooltip.TimeGraphTooltip;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

class StubModelRenderProvider extends TimeGraphModelRenderProvider {

    private static final int NB_ENTRIES = 20;

    private static final TimeGraphTreeRender TREE_RENDER;

    static {
        List<TimeGraphTreeElement> treeElements = IntStream.range(0, NB_ENTRIES)
            .mapToObj(i -> new TimeGraphTreeElement("Entry #" + i, Collections.emptyList()))
            .collect(Collectors.toList());
        TREE_RENDER = new TimeGraphTreeRender(treeElements);
    }

    protected StubModelRenderProvider() {
        super(null, null);
    }

    @Override
    public TimeGraphTreeRender getTreeRender() {
        return TREE_RENDER;
    }

    @Override
    public TimeGraphStateRender getStateRender(TimeGraphTreeElement treeElement,
            TimeRange timeRange, long resolution, @Nullable FutureTask<?> task) {
        return new TimeGraphStateRender(timeRange, treeElement, Collections.EMPTY_LIST);
    }

    @Override
    public @NonNull TimeGraphDrawnEventRender getDrawnEventRender(
            TimeGraphTreeElement treeElement, TimeRange timeRange) {
        return new TimeGraphDrawnEventRender();
    }

    @Override
    public @NonNull TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender) {
        return new TimeGraphArrowRender();
    }

    @Override
    public @NonNull TimeGraphTooltip getTooltip(TimeGraphStateInterval interval) {
        return new TimeGraphTooltip();
    }

}
