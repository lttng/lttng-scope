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
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.TimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval.LineThickness;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tooltip.TimeGraphTooltip;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.StateSystemUtils;
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
        public final long renderTimeRangeStart;
        public final long renderTimeRangeEnd;
        public final List<ITmfStateInterval> fullQueryAtRangeStart;

        public TreeRenderContext(ITmfStateSystem ss,
                SortingMode sortingMode,
                Set<FilterMode> filterModes,
                long renderTimeRangeStart,
                long renderTimeRangeEnd,
                List<ITmfStateInterval> fullQueryAtRangeStart) {
            this.ss = ss;
            this.sortingMode = sortingMode;
            this.filterModes = filterModes;
            this.renderTimeRangeStart = renderTimeRangeStart;
            this.renderTimeRangeEnd = renderTimeRangeEnd;
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

    private final String fStateSystemModuleId;
    private final Function<TreeRenderContext, TimeGraphTreeRender> fTreeRenderFunction;
    private final Function<StateIntervalContext, TimeGraphStateInterval> fIntervalMappingFunction;

//    private final Map<ITmfStateSystem, TimeGraphTreeRender> fLastTreeRenders = new WeakHashMap<>();

    /**
     * @param stateSystemModuleId
     * @param stateNameMappingFunction
     * @param colorMappingFunction
     * @param lineThicknessMappingFunction
     * @param propertyMappingFunction
     * @param baseQuarkPattern
     */
    protected StateSystemModelRenderProvider(
            @Nullable List<SortingMode> sortingModes,
            @Nullable List<FilterMode> filterModes,
            String stateSystemModuleId,
            Function<TreeRenderContext, TimeGraphTreeRender> treeRenderFunction,
            Function<StateIntervalContext, String> stateNameMappingFunction,
            Function<StateIntervalContext, ColorDefinition> colorMappingFunction,
            Function<StateIntervalContext, LineThickness> lineThicknessMappingFunction) {

        super(sortingModes, filterModes);

        fStateSystemModuleId = stateSystemModuleId;
        fTreeRenderFunction = treeRenderFunction;

        fIntervalMappingFunction = ssCtx -> {
            return new TimeGraphStateInterval(
                    ssCtx.sourceInterval.getStartTime(),
                    ssCtx.sourceInterval.getEndTime(),
                    ssCtx.baseTreeElement,
                    stateNameMappingFunction.apply(ssCtx),
                    colorMappingFunction.apply(ssCtx),
                    lineThicknessMappingFunction.apply(ssCtx));
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
    public @NonNull TimeGraphTreeRender getTreeRender(long startTime, long endTime) {
        TimeGraphTreeRender lastRender = null;
//        TimeGraphTreeRender lastRender = fLastTreeRenders.get(ss);
//        if (lastRender != null && lastRender.getAllTreeElements().size() == ss.getNbAttributes()) {
//            /* The last render is still valid, we can re-use it */
//            return lastRender;
//        }

        ITmfStateSystem ss = getSSOfCurrentTrace();
        if (ss == null) {
            /* This trace does not provide the expected state system */
            return TimeGraphTreeRender.EMPTY_RENDER;
        }


        /* First generate the tree render context */
        List<ITmfStateInterval> fullStateAtStart;
        try {
            fullStateAtStart = ss.queryFullState(startTime);
        } catch (StateSystemDisposedException e) {
            return TimeGraphTreeRender.EMPTY_RENDER;
        }

        TreeRenderContext treeContext = new TreeRenderContext(ss,
                getCurrentSortingMode(),
                getActiveFilterModes(),
                startTime,
                endTime,
                fullStateAtStart);

        /* Generate a new tree render */
        lastRender = fTreeRenderFunction.apply(treeContext);

//        fLastTreeRenders.put(ss, lastRender);
        return lastRender;
    }

    @Override
    public TimeGraphStateRender getStateRender(TimeGraphTreeElement treeElement,
            long rangeStart, long rangeEnd, long resolution) {

        ITmfStateSystem ss = getSSOfCurrentTrace();
        if (ss == null) {
            /* Has been called with an invalid trace/treeElement */
            throw new IllegalArgumentException();
        }

        // FIXME Add generic type?
        StateSystemTimeGraphTreeElement treeElem = (StateSystemTimeGraphTreeElement) treeElement;

        /* Prepare the state intervals */
        /*
         * FIXME Inefficient series of queryHistoryRange() calls, replace with a
         * 2D query once those become available.
         */
        List<ITmfStateInterval> intervals;
        try {
            intervals = StateSystemUtils.queryHistoryRange(ss, treeElem.getSourceQuark(), rangeStart, rangeEnd, resolution);
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            intervals = Collections.emptyList();
            e.printStackTrace();
        }

        List<TimeGraphStateInterval> stateIntervals = intervals.stream()
                .map(interval -> {
                    List<ITmfStateInterval> fullState;
                    try {
                        fullState = ss.queryFullState(interval.getStartTime());
                    } catch (StateSystemDisposedException e) {
                        fullState = Collections.emptyList();
                        e.printStackTrace();
                    }
                    return new StateIntervalContext(ss, treeElem, interval, fullState);
                })
                .map(fIntervalMappingFunction)
                .collect(Collectors.toList());

        return new TimeGraphStateRender(rangeStart, rangeEnd, treeElement, stateIntervals);
    }

    @Override
    public TimeGraphDrawnEventRender getDrawnEventRender(
            TimeGraphTreeElement treeElement, long rangeStart, long rangeEnd) {
        // TODO
        return new TimeGraphDrawnEventRender();
    }

    @Override
    public TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender) {
        // TODO
        return new TimeGraphArrowRender();
    }

    @Override
    public TimeGraphTooltip getTooltip(TimeGraphStateInterval interval) {
        // TODO
        return new TimeGraphTooltip();
    }
}
