/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.render.drawnevents;

import com.efficios.jabberwocky.common.ConfigOption;
import com.efficios.jabberwocky.views.common.ColorDefinition;
import com.efficios.jabberwocky.views.common.EventSymbolStyle;

/**
 * Definition of a time graph arrow series.
 *
 * It contains all the styling information (symbols, color, etc.) for the events
 * that are part of this series. It does not contain the events themselves, the
 * events will keep a reference to their series instead.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphDrawnEventSeries {

    private final String fSeriesName;
    private final ConfigOption<ColorDefinition> fColor;
    private final ConfigOption<EventSymbolStyle> fSymbolStyle;

    /**
     * Constructor.
     *
     * The parameters passed as {@link ConfigOption}s can be modified at
     * runtime.
     *
     * @param seriesName
     *            Name of this event series
     * @param color
     *            Color for this event series.
     * @param symbolStyle
     *            Symbol style for this series
     */
    public TimeGraphDrawnEventSeries(String seriesName,
            ConfigOption<ColorDefinition> color,
            ConfigOption<EventSymbolStyle> symbolStyle) {

        fSeriesName = seriesName;
        fColor = color;
        fSymbolStyle = symbolStyle;
    }

    /**
     * Get the name of this series
     *
     * @return This series's name
     */
    public String getSeriesName() {
        return fSeriesName;
    }

    /**
     * Get the configurable color of this series.
     *
     * @return This series's color
     */
    public ConfigOption<ColorDefinition> getColor() {
        return fColor;
    }

    /**
     * Get the configurable symbol style of this series.
     *
     * @return This series's symbol style
     */
    public ConfigOption<EventSymbolStyle> getSymbolStyle() {
        return fSymbolStyle;
    }

}
