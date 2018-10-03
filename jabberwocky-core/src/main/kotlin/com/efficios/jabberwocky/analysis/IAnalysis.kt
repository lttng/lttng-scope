/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.analysis

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.project.TraceProject

interface IAnalysis {

    companion object {
        internal const val ANALYSES_DIRECTORY = "analyses"
    }

    /**
     * Determine if the current analysis can run on the given trace project.

     * If it does not apply, then it should not be suggested for the given project
     * at all.

     * This method should be a quick filter, and should not for instance call
     * external processes.

     * @param project The trace project to check for
     * @return If this analysis applies to the project
     */
    fun appliesTo(project: TraceProject<*, *>): Boolean

    /**
     * Second level of checking if an analysis can run on a trace project.
     *
     * A analysis that [.appliesTo] a project but whose [.canExecute]
     * returns false should be suggested to the user, albeit unavailable. For
     * example, striked-out in the UI.
     *
     * This will indicate to the user that in normal cases this analysis should
     * work, but something (trace contents, environment, etc.) is preventing it.
     *
     * @param project The trace project to check for
     * @return If this analysis can be run on this project
     */
    fun canExecute(project: TraceProject<*, *>): Boolean

    /**
     * Execute the analysis on the given trace project.
     *
     * It should have been ensured that the analysis can run first on this
     * project, by calling both [.appliesTo] and [.canExecute].
     *
     * @param project The trace project on which to execute the analysis
     * @param range The timerange on which to execute the analysis. Omitting it will run
     *              the analysis on the whole valid time range of the project.
     * @param extraParams Optional user-defined parameters to add to the analysis's command.
     * @return The results of this analysis. Exact object type is analysis-dependent, a
     * *            more specific return type is encouraged.
     */
    fun execute(project: TraceProject<*, *>, range: TimeRange? = null, extraParams: String? = null): Any
}