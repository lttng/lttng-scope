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
import org.lttng.scope.tmf2.views.core.MathUtils;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.TimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowSeries;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.BasicTimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.MultiStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval.LineThickness;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.google.common.collect.Iterables;

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
            @Nullable List<TimeGraphArrowSeries> arrowSeries,
            String stateSystemModuleId,
            Function<TreeRenderContext, TimeGraphTreeRender> treeRenderFunction,
            Function<StateIntervalContext, String> stateNameMappingFunction,
            Function<StateIntervalContext, @Nullable String> labelMappingFunction,
            Function<StateIntervalContext, ColorDefinition> colorMappingFunction,
            Function<StateIntervalContext, LineThickness> lineThicknessMappingFunction,
            Function<StateIntervalContext, Map<String, String>> propertiesMappingFunction) {

        super(name, sortingModes, filterModes, arrowSeries);

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
    public TimeGraphArrowRender getArrowRender(TimeGraphArrowSeries series, TimeRange timeRange) {
        // TODO
        return new TimeGraphArrowRender(timeRange, Collections.EMPTY_LIST);
    }

    private List<TimeGraphStateInterval> queryHistoryRange(ITmfStateSystem ss,
            StateSystemTimeGraphTreeElement treeElem,
            final long t1, final long t2, final long resolution,
            @Nullable FutureTask<?> task)
            throws AttributeNotFoundException, StateSystemDisposedException {

        /* Validate the parameters. */
        if (t2 < t1 || resolution <= 0) {
            throw new IllegalArgumentException(ss.getSSID() + " Start:" + t1 + ", End:" + t2 + ", Resolution:" + resolution); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        final List<TimeGraphStateInterval> modelIntervals = new LinkedList<>();
        final int attributeQuark = treeElem.getSourceQuark();
        ITmfStateInterval lastAddedInterval = null;

        /* Actual valid end time of the range query. */
        long tEnd = Math.min(t2, ss.getCurrentEndTime());

        /*
         * First, iterate over the "resolution points" and keep all matching
         * state intervals.
         */
        for (long ts = t1; ts <= tEnd - resolution; ts += resolution) {
            /*
             * Skip queries if the corresponding interval was already included
             */
            if (lastAddedInterval != null && lastAddedInterval.getEndTime() >= ts) {
                long nextTOffset = MathUtils.roundToClosestHigherMultiple(lastAddedInterval.getEndTime() - t1, resolution);
                long nextTs = t1 + nextTOffset;
                if (nextTs == ts) {
                    /*
                     * The end time of the last interval happened to be exactly
                     * equal to the next resolution point. We will go to the
                     * resolution point after that then.
                     */
                    ts = nextTs;
                } else {
                    /* 'ts' will get incremented at next loop */
                    ts = nextTs - resolution;
                }
                continue;
            }

            ITmfStateInterval stateSystemInterval = ss.querySingleState(ts, attributeQuark);

            /*
             * Only pick the interval if it fills the current resolution range,
             * from 'ts' to 'ts + resolution' (or 'ts2').
             */
            long ts2 = ts + resolution;
            if (stateSystemInterval.getStartTime() <= ts && stateSystemInterval.getEndTime() >= ts2) {
                TimeGraphStateInterval interval = ssIntervalToModelInterval(ss, treeElem, stateSystemInterval);
                modelIntervals.add(interval);
                lastAddedInterval = stateSystemInterval;
            }
        }

        /*
         * For the very last interval, we'll use ['tEnd - resolution', 'tEnd']
         * as a range condition instead.
         */
        long ts = tEnd - resolution;
        long ts2 = tEnd;
        if (lastAddedInterval != null && lastAddedInterval.getEndTime() >= ts) {
            /* Interval already included */
        } else {
            ITmfStateInterval stateSystemInterval = ss.querySingleState(ts, attributeQuark);
            if (stateSystemInterval.getStartTime() <= ts && stateSystemInterval.getEndTime() >= ts2) {
                TimeGraphStateInterval interval = ssIntervalToModelInterval(ss, treeElem, stateSystemInterval);
                modelIntervals.add(interval);
            }
        }

        /*
         * 'modelIntervals' now contains all the "real" intervals we will want
         * to display in the view. Poly-filla the holes in between each using
         * multi-state intervals.
         */
        if (modelIntervals.size() < 2) {
            return modelIntervals;
        }

        List<TimeGraphStateInterval> filledIntervals = new LinkedList<>();
        /*
         * Add the first real interval. There might be a multi-state at the
         * beginning.
         */
        long firstRealIntervalStartTime = modelIntervals.get(0).getStartTime();
        if (firstRealIntervalStartTime > t1) {
            filledIntervals.add(new MultiStateInterval(t1, firstRealIntervalStartTime - 1, treeElem));
        }
        filledIntervals.add(modelIntervals.get(0));

        for (int i = 1; i < modelIntervals.size(); i++) {
            TimeGraphStateInterval interval1 = modelIntervals.get(i - 1);
            TimeGraphStateInterval interval2 = modelIntervals.get(i);
            long bound1 = interval1.getEndTime();
            long bound2 = interval2.getStartTime();

            /* (we've already inserted 'interval1' on the previous loop.) */
            if (bound1 + 1 != bound2) {
                TimeGraphStateInterval multiStateInterval = new MultiStateInterval(bound1 + 1, bound2 - 1, treeElem);
                filledIntervals.add(multiStateInterval);
            }
            filledIntervals.add(interval2);
        }

        /* Add a multi-state at the end too, if needed */
        long lastRealIntervalEndTime = Iterables.getLast(modelIntervals).getEndTime();
        if (lastRealIntervalEndTime < t2) {
            filledIntervals.add(new MultiStateInterval(lastRealIntervalEndTime + 1, t2, treeElem));
        }

        return filledIntervals;
    }

    private TimeGraphStateInterval ssIntervalToModelInterval(ITmfStateSystem ss,
            StateSystemTimeGraphTreeElement treeElem, ITmfStateInterval interval) {
        List<ITmfStateInterval> fullState;
        try {
            // TODO Big performance improvement low-hanging fruit here
            fullState = ss.queryFullState(interval.getStartTime());
        } catch (StateSystemDisposedException e) {
            fullState = Collections.emptyList();
            e.printStackTrace();
        }
        StateIntervalContext siCtx = new StateIntervalContext(ss, treeElem, interval, fullState);
        return fIntervalMappingFunction.apply(siCtx);
    }

}
