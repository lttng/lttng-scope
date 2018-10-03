/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider.drawnevents;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

/**
 * Manager of {@link TimeGraphDrawnEventProvider}s.
 *
 * @author Alexandre Montplaisir
 */
public final class TimeGraphDrawnEventProviderManager {

    private static final Comparator<TimeGraphDrawnEventProvider> COMPARATOR =
            Comparator.comparing(provider -> provider.getDrawnEventSeries().getSeriesName());

    private static final TimeGraphDrawnEventProviderManager INSTANCE = new TimeGraphDrawnEventProviderManager();

    private final ObservableSet<TimeGraphDrawnEventProvider> fRegisteredProviders =
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
    public ObservableSet<TimeGraphDrawnEventProvider> getRegisteredProviders() {
        return fRegisteredProviders;
    }

}
