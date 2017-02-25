/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.TimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tooltip.TimeGraphTooltip;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

public class ModelRenderProviderStub extends TimeGraphModelRenderProvider {

    protected ModelRenderProviderStub() {
        super(null, null);
    }

    @Override
    public TimeGraphTreeRender getTreeRender(long startTime, long endTime) {
        return TimeGraphTreeRender.EMPTY_RENDER;
    }

    @Override
    public TimeGraphStateRender getStateRender(TimeGraphTreeElement treeElement,
            long rangeStart, long rangeEnd, long resolution) {
        return new TimeGraphStateRender(rangeStart, rangeEnd, treeElement, Collections.EMPTY_LIST);
    }

    @Override
    public @NonNull TimeGraphDrawnEventRender getDrawnEventRender(
            TimeGraphTreeElement treeElement, long rangeStart, long rangeEnd) {
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
