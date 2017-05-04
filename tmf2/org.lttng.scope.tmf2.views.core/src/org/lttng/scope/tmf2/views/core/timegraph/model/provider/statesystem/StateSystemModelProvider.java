/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.TimeGraphModelProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.arrows.ITimeGraphModelArrowProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.states.ITimeGraphModelStateProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;

/**
 * Basic implementation of a {@link TimeGraphModelProvider} backed by a state
 * system.
 *
 * @author Alexandre Montplaisir
 */
public abstract class StateSystemModelProvider extends TimeGraphModelProvider {

    /**
     * The context of a tree render. Should contain all the information to
     * generate the corresponding tree render, according to all configuration
     * options like sorting, filtering etc. specified by the user.
     */
    protected static final class TreeRenderContext {

        /** State system */
        public final ITmfStateSystem ss;
        /** Sorting mode */
        public final SortingMode sortingMode;
        /** Filter modes */
        public final Set<FilterMode> filterModes;
        /** Full query */
        public final List<ITmfStateInterval> fullQueryAtRangeStart;

        /**
         * Constructor
         *
         * @param ss
         *            State system
         * @param sortingMode
         *            Current sorting mode
         * @param filterModes
         *            Current filter modes
         * @param fullQueryAtRangeStart
         *            Full query at the start of the time range.
         */
        public TreeRenderContext(ITmfStateSystem ss,
                SortingMode sortingMode,
                Set<FilterMode> filterModes,
                List<ITmfStateInterval> fullQueryAtRangeStart) {
            this.ss = ss;
            this.sortingMode = sortingMode;
            this.filterModes = filterModes;
            this.fullQueryAtRangeStart = fullQueryAtRangeStart;
        }
    }

    /**
     * Class to encapsulate a cached {@link TimeGraphTreeRender}. This render
     * should never change, except if the number of attributes in the state
     * system does (for example, if queries were made before the state system
     * was done building).
     */
    private static final class CachedTreeRender {

        public final int nbAttributes;
        public final TimeGraphTreeRender treeRender;

        public CachedTreeRender(int nbAttributes, TimeGraphTreeRender treeRender) {
            this.nbAttributes = nbAttributes;
            this.treeRender = treeRender;
        }
    }

    private final String fStateSystemModuleId;
    private final Function<TreeRenderContext, TimeGraphTreeRender> fTreeRenderFunction;

    private final Map<ITmfStateSystem, CachedTreeRender> fLastTreeRenders = new WeakHashMap<>();

    private transient @Nullable ITmfStateSystem fStateSystem = null;

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
     * @param stateSystemModuleId
     *            ID of the state system from which data will be fetched
     * @param treeRenderFunction
     *            Function to generate a tree render for a given tree context
     */
    public StateSystemModelProvider(String name,
            @Nullable List<SortingMode> sortingModes,
            @Nullable List<FilterMode> filterModes,
            ITimeGraphModelStateProvider stateProvider,
            @Nullable List<ITimeGraphModelArrowProvider> arrowProviders,
            String stateSystemModuleId,
            Function<TreeRenderContext, TimeGraphTreeRender> treeRenderFunction) {

        super(name, sortingModes, filterModes, stateProvider, arrowProviders);

        fStateSystemModuleId = stateSystemModuleId;
        fTreeRenderFunction = treeRenderFunction;

        /*
         * Change listener which will take care of keeping the target state
         * system up to date.
         */
        traceProperty().addListener((obs, oldValue, newValue) -> {
            ITmfTrace trace = newValue;
            if (trace == null) {
                fStateSystem = null;
                return;
            }

            /*
             * Set the state system in another thread, so that if it blocks on
             * waitForInitialization, it does not block the application
             * thread...
             *
             * FIXME We ought to get rid of this blocking in Jabberwocky.
             */
            Thread thread = new Thread(() -> {
                fStateSystem = TmfStateSystemAnalysisModule.getStateSystem(trace, fStateSystemModuleId);
            });
            thread.start();
        });
    }

    // ------------------------------------------------------------------------
    // Render generation methods
    // ------------------------------------------------------------------------

    @Override
    public @NonNull TimeGraphTreeRender getTreeRender() {
        ITmfStateSystem ss = fStateSystem;
        if (ss == null) {
            /* This trace does not provide the expected state system */
            return TimeGraphTreeRender.EMPTY_RENDER;
        }

      CachedTreeRender cachedRender = fLastTreeRenders.get(ss);
      if (cachedRender != null && cachedRender.nbAttributes == ss.getNbAttributes()) {
          /* The last render is still valid, we can re-use it */
          return cachedRender.treeRender;
      }

        /* First generate the tree render context */
        List<ITmfStateInterval> fullStateAtStart;
        try {
            fullStateAtStart = ss.queryFullState(ss.getStartTime());
        } catch (StateSystemDisposedException e) {
            return TimeGraphTreeRender.EMPTY_RENDER;
        }

        TreeRenderContext treeContext = new TreeRenderContext(ss,
                getCurrentSortingMode(),
                getActiveFilterModes(),
                fullStateAtStart);

        /* Generate a new tree render */
        TimeGraphTreeRender treeRender = fTreeRenderFunction.apply(treeContext);

        fLastTreeRenders.put(ss, new CachedTreeRender(ss.getNbAttributes(), treeRender));
        return treeRender;
    }

}
