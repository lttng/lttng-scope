/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.states;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.config.ConfigOption;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;

import com.google.common.collect.ImmutableMap;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public abstract class TimeGraphModelStateProvider implements ITimeGraphModelStateProvider {

    private final ObjectProperty<@Nullable ITmfTrace> fTraceProperty = new SimpleObjectProperty<>(null);

    private final Map<String, ConfigOption<ColorDefinition>> fStateColorMapping;

    protected TimeGraphModelStateProvider(Map<String, ConfigOption<ColorDefinition>> stateColorMapping) {
        fStateColorMapping = ImmutableMap.copyOf(stateColorMapping);
    }

    @Override
    public ObjectProperty<@Nullable ITmfTrace> traceProperty() {
        return fTraceProperty;
    }

    @Override
    public Map<String, ConfigOption<ColorDefinition>> getStateColorMapping() {
        return fStateColorMapping;
    }

}
