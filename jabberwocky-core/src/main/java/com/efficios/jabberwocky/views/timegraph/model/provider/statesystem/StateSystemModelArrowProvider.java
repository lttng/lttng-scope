/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider.statesystem;

import ca.polymtl.dorsal.libdelorean.IStateSystemReader;
import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis;
import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.views.timegraph.model.provider.arrows.TimeGraphModelArrowProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowRender;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowSeries;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.FutureTask;

/**
 * Basic implementation of a {@link TimeGraphModelArrowProvider} backed by a
 * state system.
 *
 * @author Alexandre Montplaisir
 */
public abstract class StateSystemModelArrowProvider extends TimeGraphModelArrowProvider {

    private transient @Nullable
    IStateSystemReader fStateSystem = null;

    /**
     * Constructor
     *
     * @param arrowSeries
     *            The arrow series that will be represented by this arrow provider
     * @param stateSystemAnalysis
     *            State system analysis generating the state system used by this
     *            provider
     */
    public StateSystemModelArrowProvider(TimeGraphArrowSeries arrowSeries,
            StateSystemAnalysis stateSystemAnalysis) {
        super(arrowSeries);

        /*
         * Change listener which will take care of keeping the target state
         * system up to date.
         */
        traceProjectProperty().addListener((obs, oldValue, newValue) -> {
            TraceProject<?, ?> project = newValue;
            if (project != null
                    && stateSystemAnalysis.appliesTo(project)
                    && stateSystemAnalysis.canExecute(project)) {
                // TODO Cache this?
                fStateSystem = stateSystemAnalysis.execute(project, null, null);
            } else {
                fStateSystem = null;
            }
        });
    }

    /**
     * The state system from which the data should be fetched. This will be kept
     * in sync with the {@link #traceProjectProperty}.
     *
     * @return The target state system. It will be null if the current trace is
     *         null.
     */
    protected final @Nullable IStateSystemReader getStateSystem() {
        return fStateSystem;
    }

    @Override
    public abstract TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender, TimeRange timeRange, @Nullable FutureTask<?> task);
}
