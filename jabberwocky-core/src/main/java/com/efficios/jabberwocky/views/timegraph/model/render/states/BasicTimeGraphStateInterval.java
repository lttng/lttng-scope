/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.views.timegraph.model.render.states;

import com.efficios.jabberwocky.common.ConfigOption;
import com.efficios.jabberwocky.views.common.ColorDefinition;
import com.efficios.jabberwocky.views.timegraph.model.render.LineThickness;
import com.efficios.jabberwocky.views.timegraph.model.render.StateDefinition;
import com.efficios.jabberwocky.views.timegraph.model.render.TimeGraphEvent;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.google.common.base.MoreObjects;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Basic implementation of {@link TimeGraphStateInterval}.
 *
 * @author Alexandre Montplaisir
 */
public class BasicTimeGraphStateInterval implements TimeGraphStateInterval {

    private final TimeGraphEvent fStartEvent;
    private final TimeGraphEvent fEndEvent;

    private final StateDefinition fStateDef;
    private final @Nullable String fLabel;
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
     * @param stateDef
     *            State definition
     * @param label
     *            Label, see {@link #getLabel()}
     * @param properties
     *            Properties
     */
    public BasicTimeGraphStateInterval(long start,
            long end,
            TimeGraphTreeElement treeElement,
            StateDefinition stateDef,
            @Nullable String label,
            Map<String, String> properties) {

        if (start > end || start < 0 || end < 0) {
            throw new IllegalArgumentException();
        }

        fStartEvent = new TimeGraphEvent(start, treeElement);
        fEndEvent = new TimeGraphEvent(end, treeElement);

        fStateDef = stateDef;
        fLabel = label;
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
        return fStateDef.getName();
    }

    @Override
    public @Nullable String getLabel() {
        return fLabel;
    }

    @Override
    public ConfigOption<ColorDefinition> getColorDefinition() {
        return fStateDef.getColor();
    }

    @Override
    public ConfigOption<LineThickness> getLineThickness() {
        return fStateDef.getLineThickness();
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
        return Objects.hash(fStartEvent, fEndEvent, fStateDef, fLabel);
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
        return Objects.equals(fStartEvent, other.fStartEvent)
                && Objects.equals(fEndEvent, other.fEndEvent)
                && Objects.equals(fStateDef, other.fStateDef)
                && Objects.equals(fLabel, other.fLabel);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("start", fStartEvent.getTimestamp()) //$NON-NLS-1$
                .add("end", fEndEvent.getTimestamp()) //$NON-NLS-1$
                .add("stateDef", fStateDef) //$NON-NLS-1$
                .toString();
    }

}
