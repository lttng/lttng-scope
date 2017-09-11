/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;

/**
 * Simple class encapsulating a {@link ChangeListener} and its target property.
 * It allows disabling and re-enabling the listener, by simply detaching it from
 * its target, on demand. The calling class then only has one class to manage
 * (this handler) instead of two.
 *
 * @author Alexandre Montplaisir
 *
 * @param <T>
 *            The type of property
 */
public final class ChangeListenerHandler<T> {

    private final Property<T> fTarget;
    private final ChangeListener<T> fListener;

    /**
     * Build a new listener handler. The listener will be added to the target
     * (enabled).
     *
     * @param target
     *            The target property
     * @param listener
     *            The listener
     */
    public ChangeListenerHandler(Property<T> target, ChangeListener<T> listener) {
        fTarget = target;
        fListener = listener;
        enable();
    }

    /**
     * Attach the listener to its property, re-enabling it.
     */
    public void enable() {
        fTarget.addListener(fListener);
    }

    /**
     * Detach the listener from its property, disabling it.
     */
    public void disable() {
        fTarget.removeListener(fListener);
    }
}
