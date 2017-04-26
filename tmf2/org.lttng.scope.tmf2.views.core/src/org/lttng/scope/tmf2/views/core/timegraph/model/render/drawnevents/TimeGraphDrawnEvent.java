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
import org.lttng.scope.tmf2.views.core.config.ConfigOption;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.TimeGraphEvent;

public class TimeGraphDrawnEvent {

    public enum SymbolStyle {
        CIRCLE,
        CROSS,
        STAR,
        BANG,
        SQUARE,
        DIAMOND,
        TRIANGLE;
    }

    private final TimeGraphEvent fTimeGraphEvent;
    private final String fEventName;
    private final ConfigOption<ColorDefinition> fColor;
    private final ConfigOption<SymbolStyle> fSymbolStyle;
    private final @Nullable Supplier<Map<String, String>> fPropertySupplier;

    public TimeGraphDrawnEvent(TimeGraphEvent event,
            String eventName,
            ConfigOption<ColorDefinition> color,
            ConfigOption<SymbolStyle> style,
            @Nullable Supplier<Map<String, String>> propertySupplier) {
        fTimeGraphEvent = event;
        fEventName = eventName;
        fColor = color;
        fSymbolStyle = style;
        fPropertySupplier = propertySupplier;
    }

    public TimeGraphEvent getEvent() {
        return fTimeGraphEvent;
    }

    public String getEventName() {
        return fEventName;
    }

    public ConfigOption<ColorDefinition> getColor() {
        return fColor;
    }

    public ConfigOption<SymbolStyle> getSymbolStyle() {
        return fSymbolStyle;
    }

    public Map<String, String> getProperties() {
        Supplier<Map<String, String>> supplier = fPropertySupplier;
        if (supplier == null) {
            return Collections.EMPTY_MAP;
        }
        return supplier.get();
    }
}
