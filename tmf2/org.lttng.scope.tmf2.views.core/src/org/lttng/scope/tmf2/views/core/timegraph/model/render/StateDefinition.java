/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.render;

import org.lttng.scope.tmf2.views.core.config.ConfigOption;

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
}
