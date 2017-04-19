/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows;

import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;

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

    public enum LineStyle {
        FULL,
        DOTTED,
        DASHED;
    }

    private final String fSeriesName;
    private final ColorDefinition fColor;
    private final LineStyle fLineStyle;

    public TimeGraphArrowSeries(String seriesName, ColorDefinition color, LineStyle lineStyle) {
        fSeriesName = seriesName;
        fColor = color;
        fLineStyle = lineStyle;
    }

    public String getSeriesName() {
        return fSeriesName;
    }

    public ColorDefinition getColor() {
        return fColor;
    }

    public LineStyle getLineStyle() {
        return fLineStyle;
    }

}
