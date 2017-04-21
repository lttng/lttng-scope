/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.arrows;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowSeries;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public abstract class TimeGraphModelArrowProvider implements ITimeGraphModelArrowProvider {

    private final ObjectProperty<@Nullable ITmfTrace> fTraceProperty = new SimpleObjectProperty<>(null);
    private final BooleanProperty fEnabledProperty = new SimpleBooleanProperty(false);
    private final TimeGraphArrowSeries fArrowSeries;

    protected TimeGraphModelArrowProvider(TimeGraphArrowSeries arrowSeries) {
        fArrowSeries = arrowSeries;
    }

    @Override
    public final ObjectProperty<@Nullable ITmfTrace> traceProperty() {
        return fTraceProperty;
    }

    @Override
    public BooleanProperty enabledProperty() {
        return fEnabledProperty;
    }

    @Override
    public final TimeGraphArrowSeries getArrowSeries() {
        return fArrowSeries;
    }
}
