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
import java.util.concurrent.FutureTask;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.project.core.JabberwockyProjectManager;
import org.lttng.scope.tmf2.views.core.MathUtils;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.states.TimeGraphModelStateProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.StateDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.BasicTimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.MultiStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;

import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis;
import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.project.ITraceProject;
import com.google.common.collect.Iterables;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;

/**
 * Basic implementation of a {@link TimeGraphModelStateProvider} backed by a state
 * system.
 *
 * @author Alexandre Montplaisir
 */
public abstract class StateSystemModelStateProvider extends TimeGraphModelStateProvider {

    /**
     * This state system here is not necessarily the same as the one in the
     * {@link StateSystemModelProvider}!
     */
    private transient @Nullable ITmfStateSystem fStateSystem = null;

    /**
     * Constructor
     *
     * @param stateDefinitions
     *            The state definitions used in this provider
     * @param stateSystemAnalysis
     *            State system analysis generating the state system used by this
     *            provider
     */
    public StateSystemModelStateProvider(
            List<StateDefinition> stateDefinitions,
            StateSystemAnalysis stateSystemAnalysis) {

        super(stateDefinitions);

        /*
         * Change listener which will take care of keeping the target state
         * system up to date.
         */
        traceProjectProperty().addListener((obs, oldValue, newValue) -> {
            ITraceProject<?, ?> project = newValue;
            if (project != null
                    && stateSystemAnalysis.appliesTo(project)
                    && stateSystemAnalysis.canExecute(project)) {
                JabberwockyProjectManager mgr = JabberwockyProjectManager.instance();
                fStateSystem = (ITmfStateSystem) mgr.getAnalysisResults(project, stateSystemAnalysis);
            } else {
                fStateSystem = null;
            }
        });

    }

    /**
     * Define how this state provider generates model intervals.
     *
     * TODO All state system intervals should be queried in one go (using
     * {@link ITmfStateSystem#queryStates}), including 'interval' that is currently
     * done as a separate step.
     *
     * @param ss
     *            The target state system
     * @param treeElem
     *            The timegraph tree element (FIXME Required because of the state
     *            interval's constructor, otherwise the subclasses should only need
     *            the quark)
     * @param interval
     *            The source interval
     * @return The timegraph model interval object, you can use
     *         {@link BasicTimeGraphStateInterval} for a simple implementation.
     */
    protected abstract TimeGraphStateInterval createInterval(ITmfStateSystem ss,
            StateSystemTimeGraphTreeElement treeElem, ITmfStateInterval interval);

    // ------------------------------------------------------------------------
    // Render generation methods
    // ------------------------------------------------------------------------

    @Override
    public TimeGraphStateRender getStateRender(TimeGraphTreeElement treeElement,
            TimeRange timeRange, long resolution, @Nullable FutureTask<?> task) {

        ITmfStateSystem ss = fStateSystem;
        /*
         * Sometimes ss is null with uninitialized or empty views, just keep the model
         * empty.
         */
        if (ss == null
                || (task != null && task.isCancelled())
                /* "Title" entries should be ignored */
                || !(treeElement instanceof StateSystemTimeGraphTreeElement)) {

            return TimeGraphStateRender.EMPTY_RENDER;
        }
        StateSystemTimeGraphTreeElement treeElem = (StateSystemTimeGraphTreeElement) treeElement;

        /* Prepare the state intervals */
        List<TimeGraphStateInterval> intervals;
        try {
            intervals = queryHistoryRange(ss, treeElem,
                    timeRange.getStartTime(), timeRange.getEndTime(), resolution, task);
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            intervals = Collections.emptyList();
        }

        return new TimeGraphStateRender(timeRange, treeElement, intervals);
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

        /* Actual valid start/end time of the range query. */
        long tStart = Math.max(t1, ss.getStartTime());
        long tEnd = Math.min(t2, ss.getCurrentEndTime());

        /*
         * First, iterate over the "resolution points" and keep all matching
         * state intervals.
         */
        for (long ts = tStart; ts <= tEnd - resolution; ts += resolution) {
            /*
             * Skip queries if the corresponding interval was already included
             */
            if (lastAddedInterval != null && lastAddedInterval.getEndTime() >= ts) {
                long nextTOffset = MathUtils.roundToClosestHigherMultiple(lastAddedInterval.getEndTime() - tStart, resolution);
                long nextTs = tStart + nextTOffset;
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
                TimeGraphStateInterval interval = createInterval(ss, treeElem, stateSystemInterval);
                modelIntervals.add(interval);
                lastAddedInterval = stateSystemInterval;
            }
        }

        /*
         * For the very last interval, we'll use ['tEnd - resolution', 'tEnd']
         * as a range condition instead.
         */
        long ts = Math.max(tStart, tEnd - resolution);
        long ts2 = tEnd;
        if (lastAddedInterval != null && lastAddedInterval.getEndTime() >= ts) {
            /* Interval already included */
        } else {
            ITmfStateInterval stateSystemInterval = ss.querySingleState(ts, attributeQuark);
            if (stateSystemInterval.getStartTime() <= ts && stateSystemInterval.getEndTime() >= ts2) {
                TimeGraphStateInterval interval = createInterval(ss, treeElem, stateSystemInterval);
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
        if (firstRealIntervalStartTime > tStart) {
            filledIntervals.add(new MultiStateInterval(tStart, firstRealIntervalStartTime - 1, treeElem));
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

}
