/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.TimeGraphEvent;

public class TimeGraphDrawnEvent {

    private final TimeGraphEvent fTimeGraphEvent;
    private final TimeGraphDrawnEventSeries fEventSeries;
    private final @Nullable Supplier<Map<String, String>> fPropertySupplier;

    public TimeGraphDrawnEvent(TimeGraphEvent event,
            TimeGraphDrawnEventSeries eventSeries,
            @Nullable Supplier<Map<String, String>> propertySupplier) {
        fTimeGraphEvent = event;
        fEventSeries = eventSeries;
        fPropertySupplier = propertySupplier;
    }

    public TimeGraphEvent getEvent() {
        return fTimeGraphEvent;
    }

    public TimeGraphDrawnEventSeries getEventSeries() {
        return fEventSeries;
    }

    public Map<String, String> getProperties() {
        Supplier<Map<String, String>> supplier = fPropertySupplier;
        if (supplier == null) {
            return Collections.EMPTY_MAP;
        }
        return supplier.get();
    }
}
