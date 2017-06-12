/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.provider;

import static org.lttng.scope.common.core.NonNullUtils.nullToEmptyString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.arrows.ITimeGraphModelArrowProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.states.ITimeGraphModelStateProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Base implementation of {@link ITimeGraphModelProvider}.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TimeGraphModelProvider implements ITimeGraphModelProvider {

    /**
     * A "default" sorting mode, for use when only one is needed.
     */
    protected static final SortingMode DEFAULT_SORTING_MODE = new SortingMode(nullToEmptyString(Messages.DefaultSortingModeName));

    private final String fName;
    private final List<SortingMode> fSortingModes;
    private final List<FilterMode> fFilterModes;

    private final ITimeGraphModelStateProvider fStateProvider;
    private final List<ITimeGraphModelArrowProvider> fArrowProviders;

    private final Set<FilterMode> fActiveFilterModes = new HashSet<>();
    private SortingMode fCurrentSortingMode;

    private final ObjectProperty<@Nullable ITmfTrace> fTraceProperty = new SimpleObjectProperty<>();

    /**
     * Constructor
     *
     * @param name
     *            The name of this provider
     * @param sortingModes
     *            The available sorting modes
     * @param filterModes
     *            The available filter modes
     * @param stateProvider
     *            The state provider part of this model provider
     * @param arrowProviders
     *            The arrow provider(s) supplied by this model provider
     */
    public TimeGraphModelProvider(String name,
            @Nullable List<SortingMode> sortingModes,
            @Nullable List<FilterMode> filterModes,
            ITimeGraphModelStateProvider stateProvider,
            @Nullable List<ITimeGraphModelArrowProvider> arrowProviders) {
        fName = name;

        fStateProvider = stateProvider;
        stateProvider.traceProperty().bind(fTraceProperty);

        if (sortingModes == null || sortingModes.isEmpty()) {
            fSortingModes = ImmutableList.of(DEFAULT_SORTING_MODE);
        } else {
            fSortingModes = ImmutableList.copyOf(sortingModes);

        }
        fCurrentSortingMode = fSortingModes.get(0);

        if (filterModes == null || filterModes.isEmpty()) {
            fFilterModes = ImmutableList.of();
        } else {
            fFilterModes = ImmutableList.copyOf(filterModes);
        }

        if (arrowProviders == null || arrowProviders.isEmpty()) {
            fArrowProviders = ImmutableList.of();
        } else {
            fArrowProviders = ImmutableList.copyOf(arrowProviders);
        }
        fArrowProviders.forEach(ap -> ap.traceProperty().bind(fTraceProperty));
    }

    @Override
    public final String getName() {
        return fName;
    }

    @Override
    public final void setTrace(@Nullable ITmfTrace trace) {
        fTraceProperty.set(trace);
    }

    @Override
    public final @Nullable ITmfTrace getTrace() {
        return fTraceProperty.get();
    }

    @Override
    public final ObjectProperty<@Nullable ITmfTrace> traceProperty() {
        return fTraceProperty;
    }

    @Override
    public final ITimeGraphModelStateProvider getStateProvider() {
        return fStateProvider;
    }

    @Override
    public final List<ITimeGraphModelArrowProvider> getArrowProviders() {
        return fArrowProviders;
    }

    // ------------------------------------------------------------------------
    // Render generation methods. Implementation left to subclasses.
    // ------------------------------------------------------------------------

    @Override
    public abstract TimeGraphTreeRender getTreeRender();

    // ------------------------------------------------------------------------
    // Sorting modes
    // ------------------------------------------------------------------------

    @Override
    public final List<SortingMode> getSortingModes() {
        return fSortingModes;
    }

    @Override
    public final SortingMode getCurrentSortingMode() {
        return fCurrentSortingMode;
    }

    @Override
    public final void setCurrentSortingMode(int index) {
        fCurrentSortingMode = fSortingModes.get(index);
    }

    // ------------------------------------------------------------------------
    // Filter modes
    // ------------------------------------------------------------------------

    @Override
    public final List<FilterMode> getFilterModes() {
        return fFilterModes;
    }

    @Override
    public final void enableFilterMode(int index) {
        fActiveFilterModes.add(fFilterModes.get(index));
    }

    @Override
    public final void disableFilterMode(int index) {
        fActiveFilterModes.remove(fFilterModes.get(index));
    }

    @Override
    public final Set<FilterMode> getActiveFilterModes() {
        return ImmutableSet.copyOf(fActiveFilterModes);
    }

}
