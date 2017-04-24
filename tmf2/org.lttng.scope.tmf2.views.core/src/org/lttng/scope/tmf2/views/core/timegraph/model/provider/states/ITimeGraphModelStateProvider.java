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
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.config.ConfigOption;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import javafx.beans.property.ObjectProperty;

public interface ITimeGraphModelStateProvider {

    ObjectProperty<@Nullable ITmfTrace> traceProperty();

    Map<String, ConfigOption<ColorDefinition>> getStateColorMapping();

    TimeGraphStateRender getStateRender(TimeGraphTreeElement treeElement,
            TimeRange timeRange, long resolution, @Nullable FutureTask<?> task);

    default List<TimeGraphStateRender> getStateRenders(TimeGraphTreeRender treeRender, TimeRange timeRange, long resolution, @Nullable FutureTask<?> task) {
        return treeRender.getAllTreeElements().stream()
                .map(treeElem -> getStateRender(treeElem, timeRange, resolution, task))
                .collect(Collectors.toList());
    }

}
