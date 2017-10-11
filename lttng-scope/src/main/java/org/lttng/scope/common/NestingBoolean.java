/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class that serves as a wrapper around a single boolean flag that can
 * be enabled/disabled. It counts the number of times {@link #disable()} is
 * called, and only really re-enables the inner value when {@link #enable()} is
 * called that many times.
 *
 * It is meant to be useful in multi-thread scenarios, where concurrent
 * "critical sections" may want to disable something like a listener, and not
 * have it really be re-enabled until all critical sections are finished. Thus
 * it is thread-safe.
 *
 * The inner value is exposed through the {@link #enabledProperty()} method, which
 * returns a {@link ReadOnlyBooleanProperty}. You can attach
 * {@link ChangeListener}s to that property to get notified of inner value
 * changes.
 *
 * It is "enabled" at creation time.
 *
 * @author Alexandre Montplaisir
 */
public class NestingBoolean {

    private final AtomicInteger fDisabledCount = new AtomicInteger(0);
    private final BooleanProperty fBoolean = new SimpleBooleanProperty(true);

    /**
     * Decrease the "disabled" count by 1. If it reaches (or already was at) 0
     * then the value is truly enabled.
     */
    public synchronized void enable() {
        /* Decrement the count but only if it is currently above 0 */
        int ret = fDisabledCount.updateAndGet(value -> value > 0 ? value - 1 : 0);
        if (ret == 0) {
            fBoolean.set(true);
        }
    }

    /**
     * Increase the "disabled" count by 1. The inner value will necessarily be
     * disabled after this call.
     */
    public synchronized void disable() {
        fDisabledCount.incrementAndGet();
        fBoolean.set(false);
    }

    /**
     * Property representing the inner boolean value.
     *
     * @return The inner value
     */
    public ReadOnlyBooleanProperty enabledProperty() {
        return fBoolean;
    }
}