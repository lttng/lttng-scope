/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.render.states;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.config.ConfigOption;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.LineThickness;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.TimeGraphEvent;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;

import com.google.common.base.MoreObjects;

/**
 * Basic implementation of {@link TimeGraphStateInterval}.
 *
 * @author Alexandre Montplaisir
 */
public class BasicTimeGraphStateInterval implements TimeGraphStateInterval {

    private final TimeGraphEvent fStartEvent;
    private final TimeGraphEvent fEndEvent;

    private final String fStateName;
    private final @Nullable String fLabel;
    private final ConfigOption<ColorDefinition> fColor;
    private final ConfigOption<LineThickness> fLineThickness;

    private final Map<String, String> fProperties;

    /**
     * Constructor.
     *
     * It requires supplying the start time, end time, and tree element. The
     * corresponding {@link TimeGraphEvent} will be created from those.
     *
     * @param start
     *            Start time
     * @param end
     *            End time
     * @param treeElement
     *            Tree element of this interval
     * @param stateName
     *            State name
     * @param label
     *            Label, see {@link #getLabel()}
     * @param color
     *            Color
     * @param lineThickness
     *            Line thickness
     * @param properties
     *            Properties
     */
    public BasicTimeGraphStateInterval(long start,
            long end,
            TimeGraphTreeElement treeElement,
            String stateName,
            @Nullable String label,
            ConfigOption<ColorDefinition> color,
            ConfigOption<LineThickness> lineThickness,
            Map<String, String> properties) {

        if (start > end || start < 0 || end < 0) {
            throw new IllegalArgumentException();
        }

        fStartEvent = new TimeGraphEvent(start, treeElement);
        fEndEvent = new TimeGraphEvent(end, treeElement);

        fStateName = stateName;
        fLabel = label;
        fColor = color;
        fLineThickness = lineThickness;
        fProperties = properties;
    }

    @Override
    public TimeGraphEvent getStartEvent() {
        return fStartEvent;
    }

    @Override
    public TimeGraphEvent getEndEvent() {
        return fEndEvent;
    }

    @Override
    public String getStateName() {
        return fStateName;
    }

    @Override
    public @Nullable String getLabel() {
        return fLabel;
    }

    @Override
    public ConfigOption<ColorDefinition> getColorDefinition() {
        return fColor;
    }

    @Override
    public ConfigOption<LineThickness> getLineThickness() {
        return fLineThickness;
    }

    @Override
    public boolean isMultiState() {
        return false;
    }

    @Override
    public Map<String, String> getProperties() {
        return fProperties;
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
        BasicTimeGraphStateInterval other = (BasicTimeGraphStateInterval) obj;
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
                .add("start", fStartEvent.getTimestamp()) //$NON-NLS-1$
                .add("end", fEndEvent.getTimestamp()) //$NON-NLS-1$
                .add("name", fStateName) //$NON-NLS-1$
                .toString();
    }

}
