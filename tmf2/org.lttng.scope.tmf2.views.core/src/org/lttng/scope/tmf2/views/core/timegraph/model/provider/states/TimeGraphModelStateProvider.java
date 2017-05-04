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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.StateDefinition;

import com.google.common.collect.ImmutableList;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Basic implementation of {@link ITimeGraphModelStateProvider}.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TimeGraphModelStateProvider implements ITimeGraphModelStateProvider {

    private final ObjectProperty<@Nullable ITmfTrace> fTraceProperty = new SimpleObjectProperty<>(null);

    private final List<StateDefinition> fStateDefinitions;

    /**
     * Constructor
     *
     * @param stateDefinitions
     *            The state definitions used in this provider
     */
    public TimeGraphModelStateProvider(List<StateDefinition> stateDefinitions) {
        fStateDefinitions = ImmutableList.copyOf(stateDefinitions);
    }

    @Override
    public ObjectProperty<@Nullable ITmfTrace> traceProperty() {
        return fTraceProperty;
    }

    @Override
    public List<StateDefinition> getStateDefinitions() {
        return fStateDefinitions;
    }

}
