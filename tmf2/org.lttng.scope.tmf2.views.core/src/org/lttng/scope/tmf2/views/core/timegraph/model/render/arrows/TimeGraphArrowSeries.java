/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows;

import java.util.Collection;

import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;

import com.google.common.collect.ImmutableList;

public class TimeGraphArrowSeries {

    public enum LineStyle {
        FULL,
        DOTTED,
        DASHED;
    }

    private final String fSeriesName;
    private final ColorDefinition fColor;
    private final LineStyle fLineStyle;
    private final Collection<TimeGraphArrow> fArrows;

    public TimeGraphArrowSeries(String seriesName, ColorDefinition color,
            LineStyle lineStyle, Collection<TimeGraphArrow> arrows) {
        fSeriesName = seriesName;
        fColor = color;
        fLineStyle = lineStyle;
        fArrows = ImmutableList.copyOf(arrows);
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

    public Collection<TimeGraphArrow> getArrows() {
        return fArrows;
    }
}
