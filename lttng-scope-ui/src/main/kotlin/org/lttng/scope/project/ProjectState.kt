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
import org.lttng.scope.project.filter.EventFilterDefinition

class ProjectState(project: TraceProject<*, *>) {

    val analysisResults = ProjectAnalysisResults(project)
    val filters = ProjectFilters(project)

}

class ProjectAnalysisResults(private val project: TraceProject<*, *>) {

    private val analysisResultsMap = mutableMapOf<IAnalysis, Any>()

    /**
     * Obtain the results of the given analysis running on the given project.
     *
     * Note this method does not handle special analysis parameters, like
     * timestamps. It should only be used for "permanent" analysis results, which
     * usually run on the whole trace. For other specific analysis queries,
     * {@link IAnalysis#execute} should be called directly instead.
     *
     * @param analysis
     *            The analysis to run. Make sure it {@link IAnalysis#appliesTo} and
     *            {@link IAnalysis#canExecute} on the given project.
     * @return The results of this analysis. You will have to cast manually to the
     *         real type if you know it.
     */
    @Synchronized
    fun getAnalysisResults(analysis: IAnalysis): Any {
        var result = analysisResultsMap[analysis];
        if (result != null) {
            return result;
        }

        /* We have not run this analysis yet, let's run it and save the results */
        // TODO Separate thread/routine?
        result = analysis.execute(project, null, null);

        analysisResultsMap.put(analysis, result);
        return result;
    }

}

class ProjectFilters(private val project: TraceProject<*, *>) {

    interface FilterListener {
        fun filterCreated(filter: EventFilterDefinition)
        fun filterRemoved(filter: EventFilterDefinition)
    }

    private val registeredListeners = mutableSetOf<FilterListener>()
    private val filters = mutableSetOf<EventFilterDefinition>()

    @Synchronized
    fun registerFilterListener(listener: FilterListener) {
        /* Tell the new listener about the existing filters. */
        val added = registeredListeners.add(listener)
        if (added) filters.forEach { listener.filterCreated(it) }
    }

    @Synchronized
    fun createFilter(filter: EventFilterDefinition) {
        /* Notify registered listeners about the new filter. */
        val added = filters.add(filter)
        if (added) registeredListeners.forEach { it.filterCreated(filter) }
    }

    @Synchronized
    fun removeFilter(filter: EventFilterDefinition) {
        val removed = filters.remove(filter)
        if (removed) registeredListeners.forEach { it.filterRemoved(filter) }
    }
}
