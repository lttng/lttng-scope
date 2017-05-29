/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.statesystem;

import java.util.concurrent.FutureTask;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.arrows.TimeGraphModelArrowProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowSeries;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import com.efficios.jabberwocky.common.TimeRange;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;

/**
 * Basic implementation of a {@link TimeGraphModelArrowProvider} backed by a
 * state system.
 *
 * @author Alexandre Montplaisir
 */
public abstract class StateSystemModelArrowProvider extends TimeGraphModelArrowProvider {

    private final String fStateSystemModuleId;

    private transient @Nullable ITmfStateSystem fStateSystem = null;

    /**
     * Constructor
     *
     * @param arrowSeries
     *            The arrow series that will be represented by this arrow
     *            provider
     * @param stateSystemModuleId
     *            The ID of the state system from which the information should
     *            be fetched
     */
    public StateSystemModelArrowProvider(TimeGraphArrowSeries arrowSeries,
            String stateSystemModuleId) {
        super(arrowSeries);
        fStateSystemModuleId = stateSystemModuleId;

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

            // FIXME Remove the extra thread once we move to Jabberwocky
            Thread thread = new Thread(() -> {
                fStateSystem = TmfStateSystemAnalysisModule.getStateSystem(trace, fStateSystemModuleId);
            });
            thread.start();
        });
    }

    /**
     * The state system from which the data should be fetched. This will be kept
     * in sync with the {@link #traceProperty}.
     *
     * @return The target state system. It will be null if the current trace is
     *         null.
     */
    protected final @Nullable ITmfStateSystem getStateSystem() {
        return fStateSystem;
    }

    @Override
    public abstract TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender, TimeRange timeRange, @Nullable FutureTask<?> task);
}
