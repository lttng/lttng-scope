/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.project.core;

import java.util.HashMap;
import java.util.Map;

import com.efficios.jabberwocky.analysis.IAnalysis;
import com.efficios.jabberwocky.project.ITraceProject;

/**
 * Application-side manager that keeps track of active Jabberwocky projects, and
 * which analyses have been run on them. A kind of cache of analysis results.
 *
 * @author Alexandre Montplaisir
 */
public final class JabberwockyProjectManager {

    private static final JabberwockyProjectManager INSTANCE = new JabberwockyProjectManager();

    /**
     * Get the singleton instance of this manager.
     *
     * @return The singleton instance
     */
    public static JabberwockyProjectManager instance() {
        return INSTANCE;
    }

    private final Map<ITraceProject<?, ?>, Map<IAnalysis, Object>> fAnalysisResults = new HashMap<>();

    /**
     * Clear the "cache" for one given project. Usually should be called when said
     * project is destroyed.
     *
     * @param project The project to dispose of
     */
    public synchronized void disposeResults(ITraceProject<?, ?> project) {
        fAnalysisResults.remove(project);
    }

    /**
     * Obtain the results of the given analysis running on the given project.
     *
     * Note this method does not handle special analysis parameters, like
     * timestamps. It should only be used for "permanent" analysis results, which
     * usually run on the whole trace. For other specific analysis queries, calling
     * {@link IAnalysis#execute} should be done instead.
     *
     * @param project
     *            The project on which to run the analysis
     * @param analysis
     *            The analysis to run. Make sure it {@link IAnalysis#appliesTo} and
     *            {@link IAnalysis#canExecute} on the given project.
     * @return The results of this analysis. You will have to cast manually to the
     *         real type if you know it.
     */
    public synchronized Object getAnalysisResults(ITraceProject<?, ?> project, IAnalysis analysis) {
        Map<IAnalysis, Object> analyses = fAnalysisResults.get(project);
        if (analyses == null) {
            analyses = new HashMap<>();
            fAnalysisResults.put(project, analyses);
        }

        Object result = analyses.get(analysis);
        if (result != null) {
            return result;
        }

        /* We haven't run this analysis yet, let's run it and save the results */
        // TODO Separate thread/routine?
        result = analysis.execute(project, null, null);

        analyses.put(analysis, result);
        return result;
    }
}
