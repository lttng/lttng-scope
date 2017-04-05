/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.TimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.BasicTimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.MultiStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval.LineThickness;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;

public class StateSystemModelRenderProvider extends TimeGraphModelRenderProvider {

    /**
     * The context of a tree render. Should contain all the information to
     * generate the corresponding tree render, according to all configuration
     * options like sorting, filtering etc. specified by the user.
     */
    protected static final class TreeRenderContext {

        public final ITmfStateSystem ss;
        public final SortingMode sortingMode;
        public final Set<FilterMode> filterModes;
        public final List<ITmfStateInterval> fullQueryAtRangeStart;

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
     * The context of a single state interval. Should contain all the
     * information required to generate the state interval in the render (name,
     * color, etc.)
     */
    protected static final class StateIntervalContext {

        public final ITmfStateSystem ss;
        public final StateSystemTimeGraphTreeElement baseTreeElement;
        public final ITmfStateInterval sourceInterval;
        public final List<ITmfStateInterval> fullQueryAtIntervalStart;

        public StateIntervalContext(ITmfStateSystem ss,
                StateSystemTimeGraphTreeElement baseTreeElement,
                ITmfStateInterval sourceInterval,
                List<ITmfStateInterval> fullQueryAtIntervalStart) {
            this.ss = ss;
            this.baseTreeElement = baseTreeElement;
            this.sourceInterval = sourceInterval;
            this.fullQueryAtIntervalStart = fullQueryAtIntervalStart;
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
    private final Function<StateIntervalContext, TimeGraphStateInterval> fIntervalMappingFunction;

    private final Map<ITmfStateSystem, CachedTreeRender> fLastTreeRenders = new WeakHashMap<>();

    /**
     * @param sortingModes
     * @param filterModes
     * @param stateSystemModuleId
     * @param treeRenderFunction
     * @param stateNameMappingFunction
     * @param labelMappingFunction
     * @param colorMappingFunction
     * @param lineThicknessMappingFunction
     * @param propertiesMappingFunction
     * @param propertyMappingFunction
     * @param baseQuarkPattern
     */
    protected StateSystemModelRenderProvider(String name,
            @Nullable List<SortingMode> sortingModes,
            @Nullable List<FilterMode> filterModes,
            String stateSystemModuleId,
            Function<TreeRenderContext, TimeGraphTreeRender> treeRenderFunction,
            Function<StateIntervalContext, String> stateNameMappingFunction,
            Function<StateIntervalContext, @Nullable String> labelMappingFunction,
            Function<StateIntervalContext, ColorDefinition> colorMappingFunction,
            Function<StateIntervalContext, LineThickness> lineThicknessMappingFunction,
            Function<StateIntervalContext, Map<String, String>> propertiesMappingFunction) {

        super(name, sortingModes, filterModes);

        fStateSystemModuleId = stateSystemModuleId;
        fTreeRenderFunction = treeRenderFunction;

        fIntervalMappingFunction = ssCtx -> {
            return new BasicTimeGraphStateInterval(
                    ssCtx.sourceInterval.getStartTime(),
                    ssCtx.sourceInterval.getEndTime(),
                    ssCtx.baseTreeElement,
                    stateNameMappingFunction.apply(ssCtx),
                    labelMappingFunction.apply(ssCtx),
                    colorMappingFunction.apply(ssCtx),
                    lineThicknessMappingFunction.apply(ssCtx),
                    propertiesMappingFunction.apply(ssCtx));
        };
    }

    private @Nullable ITmfStateSystem getSSOfCurrentTrace() {
        ITmfTrace trace = getCurrentTrace();
        if (trace == null) {
            return null;
        }
        // FIXME Potentially costly to query this every time, cache it?
        return TmfStateSystemAnalysisModule.getStateSystem(trace, fStateSystemModuleId);
    }

    // ------------------------------------------------------------------------
    // Render generation methods
    // ------------------------------------------------------------------------

    @Override
    public @NonNull TimeGraphTreeRender getTreeRender() {
        ITmfStateSystem ss = getSSOfCurrentTrace();
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

    @Override
    public TimeGraphStateRender getStateRender(TimeGraphTreeElement treeElement,
            TimeRange timeRange, long resolution, @Nullable FutureTask<?> task) {

        ITmfStateSystem ss = getSSOfCurrentTrace();
        if (ss == null) {
            /* Has been called with an invalid trace/treeElement */
            throw new IllegalArgumentException();
        }

        // FIXME Add generic type?
        StateSystemTimeGraphTreeElement treeElem = (StateSystemTimeGraphTreeElement) treeElement;

        /* Prepare the state intervals */
        List<TimeGraphStateInterval> intervals;
        try {
            intervals = queryHistoryRange(ss, treeElem,
                    timeRange.getStart(), timeRange.getEnd(), resolution, task);
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            intervals = Collections.emptyList();
        }

        return new TimeGraphStateRender(timeRange, treeElement, intervals);
    }

    @Override
    public TimeGraphDrawnEventRender getDrawnEventRender(
            TimeGraphTreeElement treeElement, TimeRange timeRange) {
        // TODO
        return new TimeGraphDrawnEventRender();
    }

    @Override
    public TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender) {
        // TODO
        return new TimeGraphArrowRender();
    }

    private List<TimeGraphStateInterval> queryHistoryRange(ITmfStateSystem ss,
            StateSystemTimeGraphTreeElement treeElem, long t1, long t2, long resolution,
            @Nullable FutureTask<?> task)
            throws AttributeNotFoundException, StateSystemDisposedException {

        /* Validate the parameters. */
        if (t2 < t1 || resolution <= 0) {
            throw new IllegalArgumentException(ss.getSSID() + " Start:" + t1 + ", End:" + t2 + ", Resolution:" + resolution); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        final List<TimeGraphStateInterval> modelIntervals = new LinkedList<>();
        final int attributeQuark = treeElem.getSourceQuark();
        ITmfStateInterval currentSSInterval = null;
        boolean isInMultiState = false;
        long currentMultiStateStart = -1;
        long currentMultiStateEnd;

        /* Actual valid end time of the range query. */
        long tEnd = Math.min(t2, ss.getCurrentEndTime());

        /*
         * Iterate over the "resolution points". We skip unneeded queries in the
         * case the current interval is longer than the resolution.
         */
        for (long ts = t1;
                ts <= tEnd - resolution;
                ts += ((currentSSInterval.getEndTime() - ts) / resolution + 1) * resolution) {

            if (task != null && task.isCancelled()) {
                return modelIntervals;
            }

            currentSSInterval = ss.querySingleState(ts, attributeQuark);

            /*
             * Only pick the interval if it fills the current resolution range, from 'ts' to
             * 'ts + resolution' (or 'ts2'). If it does not, report a multi-state for this
             * pixel.
             */
            long ts2 = ts + resolution;
            if (currentSSInterval.getStartTime() <= ts
                    && currentSSInterval.getEndTime() >= ts2) {
                /* This interval fills at least the current pixel, keep it. */

                /* But first, end the ongoing multi-state if there is one. */
                if (isInMultiState) {
                    currentMultiStateEnd = currentSSInterval.getStartTime() - 1;
                    TimeGraphStateInterval multiStateInterval = new MultiStateInterval(
                            currentMultiStateStart, currentMultiStateEnd, treeElem);
                    modelIntervals.add(multiStateInterval);
                    isInMultiState = false;
                }

                TimeGraphStateInterval interval = ssIntervalToModelInterval(ss, treeElem, currentSSInterval);
                modelIntervals.add(interval);

            } else {
                /*
                 * The interval does *not* fill the full range, we'll report a "multi-state" for
                 * this pixel instead.
                 */
                if (isInMultiState) {
                    /* Extend the current multi-state */
                    currentMultiStateEnd = ts2;
                } else {
                    /* Start a new multi-state */
                    currentMultiStateStart = ts;
                    isInMultiState = true;
                }
            }
        }

        /*
         * For the very last interval, we'll use ['tEnd - resolution', 'tEnd'] as a
         * range condition instead.
         */
        long ts = tEnd -resolution;
        long ts2 = tEnd;
        currentSSInterval = ss.querySingleState(tEnd, attributeQuark);
        if (currentSSInterval.getStartTime() <= ts
                && currentSSInterval.getEndTime() >= ts2) {

            /*
             * End the ongoing multi-state if there is one.
             */
            if (isInMultiState) {
                currentMultiStateEnd = currentSSInterval.getStartTime() - 1;
                TimeGraphStateInterval multiStateInterval = new MultiStateInterval(
                        currentMultiStateStart, currentMultiStateEnd, treeElem);
                modelIntervals.add(multiStateInterval);
            }

            /* Then add the last interval, if we don't have it already. */
            TimeGraphStateInterval interval = ssIntervalToModelInterval(ss, treeElem, currentSSInterval);
            if (!interval.equals(modelIntervals.get(modelIntervals.size() - 1))) {
                    modelIntervals.add(interval);
            }

        } else {
            if (isInMultiState) {
                /* Extend the current multi-state until the end */
                currentMultiStateEnd = ts2;
            } else {
                /* Multi-state for only the last pixel. */
                currentMultiStateStart = ts;
                currentMultiStateEnd = ts2;
            }
            TimeGraphStateInterval multiStateInterval = new MultiStateInterval(
                    currentMultiStateStart, currentMultiStateEnd, treeElem);
            modelIntervals.add(multiStateInterval);
        }

        return modelIntervals;
    }

    private TimeGraphStateInterval ssIntervalToModelInterval(ITmfStateSystem ss,
            StateSystemTimeGraphTreeElement treeElem, ITmfStateInterval interval) {
        List<ITmfStateInterval> fullState;
        try {
            fullState = ss.queryFullState(interval.getStartTime());
        } catch (StateSystemDisposedException e) {
            fullState = Collections.emptyList();
            e.printStackTrace();
        }
        StateIntervalContext siCtx = new StateIntervalContext(ss, treeElem, interval, fullState);
        return fIntervalMappingFunction.apply(siCtx);
    }

}
