/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.drawnevents;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

/**
 * Manager of {@link ITimeGraphDrawnEventProvider}s.
 *
 * @author Alexandre Montplaisir
 */
public final class TimeGraphDrawnEventProviderManager {

    private static final Comparator<ITimeGraphDrawnEventProvider> COMPARATOR =
            Comparator.comparing(provider -> provider.getEventSeries().getSeriesName());

    private static final TimeGraphDrawnEventProviderManager INSTANCE = new TimeGraphDrawnEventProviderManager();

    private final ObservableSet<ITimeGraphDrawnEventProvider> fRegisteredProviders =
            FXCollections.observableSet(new TreeSet<>(COMPARATOR));

    private TimeGraphDrawnEventProviderManager() {}

    /**
     * Get the singleton instance.
     *
     * @return The instance
     */
    public static TimeGraphDrawnEventProviderManager instance() {
        return INSTANCE;
    }

    /**
     * Return the {@link ObservableSet} of registered providers.
     *
     * You can use {@link Set#add} and {@link Set#remove} to register and
     * deregister providers, but also the {@link ObservableSet#addListener}
     * methods to be notified of provider changes.
     *
     * @return The registered providers
     */
    public ObservableSet<ITimeGraphDrawnEventProvider> getRegisteredProviders() {
        return fRegisteredProviders;
    }

}
