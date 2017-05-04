/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.provider;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Manager for the time graph model providers.
 *
 * Components can register their possible time graphs, and views etc. may query
 * and instantiate them.
 *
 * @author Alexandre Montplaisir
 */
public final class TimeGraphModelProviderManager {

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

    /**
     * Register a time graph provider by specifying its
     * {@link ITimeGraphModelProviderFactory}. If a factory is already
     * registered, this method will have no effect.
     *
     * @param factory
     *            The provider's factory
     */
    public void registerProviderFactory(ITimeGraphModelProviderFactory factory) {
        fRegisteredProviderFactories.add(factory);
    }

    /**
     * Get the list of currently registered provider factories.
     *
     * @return The currently registered factories
     */
    public Iterable<ITimeGraphModelProviderFactory> getRegisteredProviderFactories() {
        return ImmutableSet.copyOf(fRegisteredProviderFactories);
    }

}
