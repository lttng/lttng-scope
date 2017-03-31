/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.render.states;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.TimeGraphEvent;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;

import com.google.common.base.MoreObjects;

public class TimeGraphStateInterval {

    public enum LineThickness {
        NORMAL,
        SMALL
    }

    private final TimeGraphEvent fStartEvent;
    private final TimeGraphEvent fEndEvent;

    private final String fStateName;
    private final @Nullable String fLabel;
    private final ColorDefinition fColor;
    private final LineThickness fLineThickness;

    public TimeGraphStateInterval(long start,
            long end,
            TimeGraphTreeElement treeElement,
            String stateName,
            @Nullable String label,
            ColorDefinition color,
            LineThickness lineThickness) {

        fStartEvent = new TimeGraphEvent(start, treeElement);
        fEndEvent = new TimeGraphEvent(end, treeElement);

        fStateName = stateName;
        fLabel = label;
        fColor = color;
        fLineThickness = lineThickness;

    }

    public TimeGraphEvent getStartEvent() {
        return fStartEvent;
    }

    public TimeGraphEvent getEndEvent() {
        return fEndEvent;
    }

    public String getStateName() {
        return fStateName;
    }

    public @Nullable String getLabel() {
        return fLabel;
    }

    public ColorDefinition getColorDefinition() {
        return fColor;
    }

    public LineThickness getLineThickness() {
        return fLineThickness;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fStartEvent, fEndEvent, fStateName, fLabel, fColor, fLineThickness);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TimeGraphStateInterval other = (TimeGraphStateInterval) obj;
        return (Objects.equals(fStartEvent, other.fStartEvent)
                && Objects.equals(fEndEvent, other.fEndEvent)
                && Objects.equals(fStateName, other.fStateName)
                && Objects.equals(fLabel, other.fLabel)
                && Objects.equals(fColor, other.fColor)
                && fLineThickness == other.fLineThickness);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("start", fStartEvent.getTimestamp())
                .add("end", fEndEvent.getTimestamp())
                .add("name", fStateName)
                .toString();
    }

}
