/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.drawnevents;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventSeries;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Basic implementation of {@link ITimeGraphDrawnEventProvider}.
 *
 * Implementation of the {@link #getEventRender} method is left to subclasses.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TimeGraphDrawnEventProvider implements ITimeGraphDrawnEventProvider {

    private final ObjectProperty<@Nullable ITmfTrace> fTraceProperty = new SimpleObjectProperty<>(null);
    private final BooleanProperty fEnabledProperty = new SimpleBooleanProperty(false);
    private final TimeGraphDrawnEventSeries fDrawnEventSeries;

    /**
     * Constructor
     *
     * @param drawnEventSeries
     *            The event series provided by this provider.
     */
    protected TimeGraphDrawnEventProvider(TimeGraphDrawnEventSeries drawnEventSeries) {
        fDrawnEventSeries = drawnEventSeries;
    }

    @Override
    public final ObjectProperty<@Nullable ITmfTrace> traceProperty() {
        return fTraceProperty;
    }

    @Override
    public final BooleanProperty enabledProperty() {
        return fEnabledProperty;
    }

    @Override
    public final TimeGraphDrawnEventSeries getEventSeries() {
        return fDrawnEventSeries;
    }

}
