/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Manager for the time graph model providers.
 *
 * Components can register their possible time graphs, and views etc. may query
 * and instantiate them.
 *
 * @author Alexandre Montplaisir
 */
public final class TimeGraphModelProviderManager {

    /**
     * Interface for classes displaying Time Graphs. Implement this interface and
     * register your class using {@link #registerOutput} to receive notifications
     * about current and newly-registered time graph providers.
     */
    public static interface TimeGraphOutput {

        /**
         * Callback called by the provider manager when model providers are registered.
         *
         * @param factory
         *            The model provider factory
         */
        void providerRegistered(ITimeGraphModelProviderFactory factory);
    }

    private static final TimeGraphModelProviderManager INSTANCE = new TimeGraphModelProviderManager();

    private TimeGraphModelProviderManager() {}

    /**
     * Get the singleton instance of this manager.
     *
     * @return The instance
     */
    public static TimeGraphModelProviderManager instance() {
        return INSTANCE;
    }

    private final Set<ITimeGraphModelProviderFactory> fRegisteredProviderFactories = new LinkedHashSet<>();
    private final Set<TimeGraphOutput> fRegisteredOutputs = new HashSet<>();

    /**
     * Register a time graph provider by specifying its
     * {@link ITimeGraphModelProviderFactory}. If a factory is already registered,
     * this method will have no effect.
     *
     * @param factory
     *            The provider's factory
     */
    public synchronized void registerProviderFactory(ITimeGraphModelProviderFactory factory) {
        boolean added = fRegisteredProviderFactories.add(factory);
        if (added) {
            fRegisteredOutputs.forEach(output -> output.providerRegistered(factory));
        }
    }

    /**
     * Register a time graph output to this manager. Upon registration,
     * notifications will be sent, using {@link TimeGraphOutput#providerRegistered},
     * for all existing providers. Additional notifications will be sent for future
     * registered providers.
     *
     * @param output
     *            The time graph output to register
     */
    public synchronized void registerOutput(TimeGraphOutput output) {
        /* Send notifications for currently-registered factories */
        fRegisteredProviderFactories.forEach(factory -> output.providerRegistered(factory));
        fRegisteredOutputs.add(output);
    }

    /**
     * Unregister a previously-registered output, so that it does not receive any
     * more notifications. Has no effect if the output was not already registered.
     *
     * @param output
     *            The output to unregister
     */
    public synchronized void unregisterOutput(TimeGraphOutput output) {
        fRegisteredOutputs.remove(output);
    }

}
