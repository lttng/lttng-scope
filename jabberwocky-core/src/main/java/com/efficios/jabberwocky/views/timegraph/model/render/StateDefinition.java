/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.render;

import com.efficios.jabberwocky.common.ConfigOption;
import com.efficios.jabberwocky.views.common.ColorDefinition;
import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * Class defining the UI reprsentation of a state, as well as default values for
 * them.
 *
 * @author Alexandre Montplaisir
 */
public class StateDefinition {

    private final String fName;
    private final ConfigOption<ColorDefinition> fColor;
    private final ConfigOption<LineThickness> fLineThickness;

    /**
     * Build a new state definition. Re-use the same object where the same
     * definition apply, so that the same configuration property is used.
     *
     * @param name
     *            The name of the definition
     * @param defaultColor
     *            The default color to use
     * @param defaultLineThickness
     *            The default line thickness to use
     */
    public StateDefinition(String name, ColorDefinition defaultColor, LineThickness defaultLineThickness) {
        fName = name;
        fColor = new ConfigOption<>(defaultColor);
        fLineThickness = new ConfigOption<>(defaultLineThickness);
    }

    /**
     * Get the name of this definition.
     *
     * @return The name
     */
    public String getName() {
        return fName;
    }

    /**
     * Get the color property of this definition.
     *
     * @return The color property
     */
    public ConfigOption<ColorDefinition> getColor() {
        return fColor;
    }

    /**
     * Get the line thickness property of this definition.
     *
     * @return The line thickness property
     */
    public ConfigOption<LineThickness> getLineThickness() {
        return fLineThickness;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fName, fColor, fLineThickness);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StateDefinition other = (StateDefinition) obj;
        return Objects.equals(fName, other.fName)
                && Objects.equals(fColor, other.fColor)
                && Objects.equals(fLineThickness, other.fLineThickness);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", fName) //$NON-NLS-1$
                .add("color", fColor) //$NON-NLS-1$
                .add("lineThickness", fLineThickness) //$NON-NLS-1$
                .toString();
    }
}
