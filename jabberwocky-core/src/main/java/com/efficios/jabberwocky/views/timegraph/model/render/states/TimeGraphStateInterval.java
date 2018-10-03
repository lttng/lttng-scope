/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.render.states;

import com.efficios.jabberwocky.common.ConfigOption;
import com.efficios.jabberwocky.views.common.ColorDefinition;
import com.efficios.jabberwocky.views.timegraph.model.render.LineThickness;
import com.efficios.jabberwocky.views.timegraph.model.render.TimeGraphEvent;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Base interface for time graph state interval, which is defined by a start
 * time, end time, and a state.
 *
 * @author Alexandre Montplaisir
 */
public interface TimeGraphStateInterval {

    /**
     * Return the start event of this interval.
     *
     * @return The start event
     */
    TimeGraphEvent getStartEvent();

    /**
     * Return the end event of this interval.
     *
     * @return The end event
     */
    TimeGraphEvent getEndEvent();

    /**
     * Get the name of the state represented by this interval.
     *
     * @return The state name
     */
    String getStateName();

    /**
     * Get the label of this interval. This means the text that is meant to be
     * displayed alongside the interval's color. It may or may not be the same
     * as the {@link #getStateName()}.
     *
     * @return The optional label of this interval
     */
    @Nullable String getLabel();

    /**
     * Get the color suggested for this interval.
     *
     * @return The color of this interval
     */
    ConfigOption<ColorDefinition> getColorDefinition();

    /**
     * Get the suggested line thickness of this interval.
     *
     * @return The line thickness
     */
    ConfigOption<LineThickness> getLineThickness();

    /**
     * Indicate if this interval represents multiple states, or a single one.
     *
     * @return If this interval is a multi-state one
     */
    boolean isMultiState();

    /**
     * Get the properties associated with this state interval. This is extra,
     * generic data that can be attached to the interval. Views can display
     * those in a tooltip, for example.
     *
     * @return The additional properties.
     */
    Map<String, String> getProperties();

    /**
     * Get the start time of this interval, which is effectively the timestamp
     * of the start event.
     *
     * @return The start time
     */
    default long getStartTime() {
        return getStartEvent().getTimestamp();
    }

    /**
     * Get the end time of this interval, which is effectively the timestamp of
     * the end event.
     *
     * @return The end time
     */
    default long getEndTime() {
        return getEndEvent().getTimestamp();
    }

    /**
     * Get the duration of this interval, effectively end time minus start time.
     *
     * @return The duration
     */
    default long getDuration() {
        return (getEndTime() - getStartTime());
    }

    /**
     * Get the tree element of this interval's start and end events.
     *
     * @return The tree element
     */
    default TimeGraphTreeElement getTreeElement() {
        return getStartEvent().getTreeElement();
    }

}
