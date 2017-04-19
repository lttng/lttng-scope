/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.states;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public abstract class TimeGraphModelStateProvider implements ITimeGraphModelStateProvider {

    private final ObjectProperty<@Nullable ITmfTrace> fTraceProperty = new SimpleObjectProperty<>(null);

    @Override
    public ObjectProperty<@Nullable ITmfTrace> traceProperty() {
        return fTraceProperty;
    }

}
