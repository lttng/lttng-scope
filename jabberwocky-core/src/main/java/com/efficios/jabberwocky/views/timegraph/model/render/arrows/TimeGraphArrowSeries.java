/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.render.arrows;

import com.efficios.jabberwocky.views.common.ColorDefinition;

/**
 * Definition of a time graph arrow series.
 *
 * It contains all the styling information (name, color, line type, etc.) for
 * the arrows part of this series. It does not however contain the arrows
 * themselves, it is just the definition. The arrows will keep reference of what
 * series they belong to.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphArrowSeries {

    /**
     * Headless definition of the style of the arrow line.
     */
    public enum LineStyle {
        /** Full line */
        FULL,
        /** Line composed of dotsf */
        DOTTED,
        /** Line composed of dashes */
        DASHED;
    }

    private final String fSeriesName;
    private final ColorDefinition fColor;
    private final LineStyle fLineStyle;

    /**
     * Constructor
     *
     * @param seriesName
     *            Name of this series
     * @param color
     *            Suggested color for arrows of this series
     * @param lineStyle
     *            Suggested line style for arrows of this series
     */
    public TimeGraphArrowSeries(String seriesName, ColorDefinition color, LineStyle lineStyle) {
        fSeriesName = seriesName;
        fColor = color;
        fLineStyle = lineStyle;
    }

    /**
     * Get the name of this series.
     *
     * @return The series's name
     */
    public String getSeriesName() {
        return fSeriesName;
    }

    /**
     * Get the suggested color of this series.
     *
     * @return The series's color
     */
    public ColorDefinition getColor() {
        return fColor;
    }

    /**
     * Get the suggested line style of this series.
     *
     * @return The series's line style
     */
    public LineStyle getLineStyle() {
        return fLineStyle;
    }

}
