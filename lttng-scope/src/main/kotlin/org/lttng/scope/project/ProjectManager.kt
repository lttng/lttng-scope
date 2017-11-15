/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.project

import com.efficios.jabberwocky.analysis.IAnalysis
import com.efficios.jabberwocky.project.TraceProject

/**
 * Application-side manager that keeps track of active Jabberwocky projects, and
 * the viewer-side state that we want to associate to them.
 */
object ProjectManager {

    private val analysisResults = mutableMapOf<TraceProject<*, *>, MutableMap<IAnalysis, Any>>();

    /**
     * Clear the "cache" for one given project. Usually should be called when said
     * project is destroyed.
     *
     * @param project The project to dispose of
     */
    @Synchronized
    fun dispose(project: TraceProject<*, *>) {
        analysisResults.remove(project);
    }

    /**
     * Obtain the results of the given analysis running on the given project.
     *
     * Note this method does not handle special analysis parameters, like
     * timestamps. It should only be used for "permanent" analysis results, which
     * usually run on the whole trace. For other specific analysis queries,
     * {@link IAnalysis#execute} should be called directly instead.
     *
     * @param project
     *            The project on which to run the analysis
     * @param analysis
     *            The analysis to run. Make sure it {@link IAnalysis#appliesTo} and
     *            {@link IAnalysis#canExecute} on the given project.
     * @return The results of this analysis. You will have to cast manually to the
     *         real type if you know it.
     */
    @Synchronized
    fun getAnalysisResults(project: TraceProject<*, *>, analysis: IAnalysis): Any {
        var analyses = analysisResults[project];
        if (analyses == null) {
            analyses = mutableMapOf()
            analysisResults.put(project, analyses);
        }

        var result = analyses[analysis];
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
