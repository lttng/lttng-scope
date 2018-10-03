/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.views.timegraph.model.provider.statesystem;

import ca.polymtl.dorsal.libdelorean.IStateSystemReader;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis;
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.views.timegraph.model.provider.TimeGraphModelProvider;
import com.efficios.jabberwocky.views.timegraph.model.provider.arrows.TimeGraphModelArrowProvider;
import com.efficios.jabberwocky.views.timegraph.model.provider.states.TimeGraphModelStateProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;

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
    public static final class TreeRenderContext {

        /** Trace name */
        public final String traceName;
        /** State system */
        public final IStateSystemReader ss;
        /** Sorting mode */
        public final SortingMode sortingMode;
        /** Filter modes */
        public final Set<FilterMode> filterModes;
        /** Full query */
        public final List<StateInterval> fullQueryAtRangeStart;

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
        public TreeRenderContext(String traceName,
                                 IStateSystemReader ss,
                                 SortingMode sortingMode,
                                 Set<FilterMode> filterModes,
                                 List<StateInterval> fullQueryAtRangeStart) {
            this.traceName = traceName;
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

    private final Function<TreeRenderContext, TimeGraphTreeRender> fTreeRenderFunction;
    private final Map<IStateSystemReader, CachedTreeRender> fLastTreeRenders = new WeakHashMap<>();

    private transient @Nullable IStateSystemReader fStateSystem = null;

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
     * @param stateSystemAnalysis
     *            State system analysis generating the state system used by this
     *            provider
     * @param treeRenderFunction
     *            Function to generate a tree render for a given tree context
     */
    public StateSystemModelProvider(String name,
            @Nullable List<SortingMode> sortingModes,
            @Nullable List<FilterMode> filterModes,
            TimeGraphModelStateProvider stateProvider,
            @Nullable List<TimeGraphModelArrowProvider> arrowProviders,
            StateSystemAnalysis stateSystemAnalysis,
            Function<TreeRenderContext, TimeGraphTreeRender> treeRenderFunction) {

        super(name, sortingModes, filterModes, stateProvider, arrowProviders);

        fTreeRenderFunction = treeRenderFunction;

        /*
         * Change listener which will take care of keeping the target state
         * system up to date.
         */
        traceProjectProperty().addListener((obs, oldValue, newProject) -> {
            if (newProject != null
                    && stateSystemAnalysis.appliesTo(newProject)
                    && stateSystemAnalysis.canExecute(newProject)) {
                // TODO Cache this?
                fStateSystem = stateSystemAnalysis.execute(newProject, null, null);
            } else {
                fStateSystem = null;
            }
        });
    }

    public @Nullable IStateSystemReader getStateSystem() {
        return fStateSystem;
    }

    // ------------------------------------------------------------------------
    // Render generation methods
    // ------------------------------------------------------------------------

    @Override
    public @NotNull TimeGraphTreeRender getTreeRender() {
        IStateSystemReader ss = fStateSystem;
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
        List<StateInterval> fullStateAtStart;
        try {
            fullStateAtStart = ss.queryFullState(ss.getStartTime());
        } catch (StateSystemDisposedException e) {
            return TimeGraphTreeRender.EMPTY_RENDER;
        }

        TraceProject<?, ?> traceProject = getTraceProject();
        String traceName = (traceProject == null ? "" : traceProject.getName()); //$NON-NLS-1$

        TreeRenderContext treeContext = new TreeRenderContext(traceName,
                ss,
                getCurrentSortingMode(),
                getActiveFilterModes(),
                fullStateAtStart);

        /* Generate a new tree render */
        TimeGraphTreeRender treeRender = fTreeRenderFunction.apply(treeContext);

        fLastTreeRenders.put(ss, new CachedTreeRender(ss.getNbAttributes(), treeRender));
        return treeRender;
    }

}
