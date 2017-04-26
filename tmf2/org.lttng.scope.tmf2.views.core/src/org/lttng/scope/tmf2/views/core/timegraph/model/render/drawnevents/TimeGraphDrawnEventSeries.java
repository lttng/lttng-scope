/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents;

import org.lttng.scope.tmf2.views.core.config.ConfigOption;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;

public class TimeGraphDrawnEventSeries {

    public enum SymbolStyle {
        CIRCLE,
        CROSS,
        STAR,
        BANG,
        SQUARE,
        DIAMOND,
        TRIANGLE;
    }

    private final String fSeriesName;
    private final ConfigOption<ColorDefinition> fColor;
    private final ConfigOption<SymbolStyle> fSymbolStyle;

    public TimeGraphDrawnEventSeries(String seriesName,
            ConfigOption<ColorDefinition> color,
            ConfigOption<SymbolStyle> symbolStyle) {

        fSeriesName = seriesName;
        fColor = color;
        fSymbolStyle = symbolStyle;
    }

    public String getSeriesName() {
        return fSeriesName;
    }

    public ConfigOption<ColorDefinition> getColor() {
        return fColor;
    }

    public ConfigOption<SymbolStyle> getSymbolStyle() {
        return fSymbolStyle;
    }

}
