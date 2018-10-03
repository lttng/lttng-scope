/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.views.timegraph.model.render.drawnevents;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import com.efficios.jabberwocky.views.timegraph.model.render.TimeGraphEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Model representation of a drawn event.
 *
 * A drawn event is a UI representation of a single {@link TimeGraphEvent}, with
 * a given style.
 *
 * Additional properties can also be part of the drawn event, for example to be
 * shown on mouse-over.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphDrawnEvent {

    private final TimeGraphEvent fTimeGraphEvent;
    private final TimeGraphDrawnEventSeries fEventSeries;
    private final @Nullable Supplier<Map<String, String>> fPropertySupplier;

    /**
     * Constructor
     *
     * @param event
     *            Time graph event (location)
     * @param eventSeries
     *            Series of this event, which contains all the styling
     *            information
     * @param propertySupplier
     *            Supplier of additional properties, which can be accessed only
     *            the first time {@link #getProperties()} is called.
     */
    public TimeGraphDrawnEvent(TimeGraphEvent event,
            TimeGraphDrawnEventSeries eventSeries,
            @Nullable Supplier<Map<String, String>> propertySupplier) {
        fTimeGraphEvent = event;
        fEventSeries = eventSeries;
        fPropertySupplier = propertySupplier;
    }

    /**
     * Get the timegraph event wrapped by this drawn event.
     *
     * @return The time graph event
     */
    public TimeGraphEvent getEvent() {
        return fTimeGraphEvent;
    }

    /**
     * Get the event series of this drawn event.
     *
     * @return The event's series
     */
    public TimeGraphDrawnEventSeries getEventSeries() {
        return fEventSeries;
    }

    /**
     * Get the additional properties of this drawn event.
     *
     * @return The event's properties
     */
    public Map<String, String> getProperties() {
        Supplier<Map<String, String>> supplier = fPropertySupplier;
        if (supplier == null) {
            return Collections.EMPTY_MAP;
        }
        return supplier.get();
    }
}
