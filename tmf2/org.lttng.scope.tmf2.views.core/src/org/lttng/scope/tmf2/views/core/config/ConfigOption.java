/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.config;

import javafx.beans.property.SimpleObjectProperty;

/**
 * Configuration option, which is basically a JavaFX property with a default
 * value.
 *
 * @author Alexandre Montplaisir
 *
 * @param <T>
 *            The type of property
 */
public class ConfigOption<T> extends SimpleObjectProperty<T> {

    private final T fDefaultValue;

    /**
     * Constructor
     *
     * @param defaultValue
     *            The initial and default value
     */
    public ConfigOption(T defaultValue) {
        super(defaultValue);
        fDefaultValue = defaultValue;
    }

    /**
     * Retrieve the default value of this option. This does not modify the
     * current value.
     *
     * @return The default value
     */
    public T getDefaultValue() {
        return fDefaultValue;
    }

    /**
     * Reset the current value to its default.
     */
    public void resetToDefault() {
        set(fDefaultValue);
    }

}
