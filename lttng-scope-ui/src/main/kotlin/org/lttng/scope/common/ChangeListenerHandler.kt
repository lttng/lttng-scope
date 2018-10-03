/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common

import javafx.beans.property.Property
import javafx.beans.value.ChangeListener

/**
 * Simple class encapsulating a {@link ChangeListener} and its target property.
 * It allows disabling and re-enabling the listener, by simply detaching it from
 * its target, on demand. The calling class then only has one class to manage
 * (this handler) instead of two.
 *
 * The listener will be added to the target (enabled) initially.
 *
 * @param <T>
 *            The type of property
 */
class ChangeListenerHandler<T>(private val target: Property<T>,
                               private val listener: ChangeListener<T>) {

    init {
        enable()
    }

    /** Attach the listener to its property, re-enabling it. */
    fun enable() = target.addListener(listener)

    /** Detach the listener from its property, disabling it. */
    fun disable() = target.removeListener(listener)
}
