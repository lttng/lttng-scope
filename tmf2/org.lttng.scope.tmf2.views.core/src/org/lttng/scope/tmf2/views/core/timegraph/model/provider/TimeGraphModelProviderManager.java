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

public final class TimeGraphModelProviderManager {

    private static final TimeGraphModelProviderManager INSTANCE = new TimeGraphModelProviderManager();

    private TimeGraphModelProviderManager() {}

    public static TimeGraphModelProviderManager instance() {
        return INSTANCE;
    }

    private final Set<ITimeGraphModelProviderFactory> fRegisteredProviderFactories = new LinkedHashSet<>();

    public void registerProviderFactory(ITimeGraphModelProviderFactory factory) {
        fRegisteredProviderFactories.add(factory);
    }

    public Iterable<ITimeGraphModelProviderFactory> getRegisteredProviderFactories() {
        return ImmutableSet.copyOf(fRegisteredProviderFactories);
    }

}
