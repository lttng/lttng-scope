/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.layer.drawnevents

import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.timegraph.model.provider.drawnevents.TimeGraphDrawnEventProviderManager
import org.lttng.scope.project.ProjectFilters
import org.lttng.scope.project.ProjectManager
import org.lttng.scope.project.filter.EventFilterDefinition
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget

/**
 * Filter listener that will listen to project filter creation/removal, and will create/remove
 * corresponding drawn event providers for this timegraph.
 */
class DrawnEventFilterListener(private val timeGraphWidget: TimeGraphWidget) : ProjectFilters.FilterListener {

    private val drawnEventProviderManager = TimeGraphDrawnEventProviderManager.instance()

    private val createdProviders = mutableMapOf<EventFilterDefinition, PredicateDrawnEventProvider>()

    private val projectChangeListener = object : ViewGroupContext.ProjectChangeListener(this) {
        override fun newProjectCb(newProject: TraceProject<*, *>?) {
            /* On project change, clear the current providers. */
            createdProviders.values.forEach { drawnEventProviderManager.registeredProviders.remove(it) }
            createdProviders.clear()

            /* Re-register to the new project, if there is one. */
            newProject?.let { ProjectManager.getProjectState(it).filters.registerFilterListener(this@DrawnEventFilterListener) }
        }
    }

    init {
        /* Initialize with current project, and attach listener for project changes. */
        timeGraphWidget.viewContext.registerProjectChangeListener(projectChangeListener)
                /* Register initial project, if there is one. */
                ?.let { ProjectManager.getProjectState(it).filters.registerFilterListener(this@DrawnEventFilterListener) }

    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        timeGraphWidget.viewContext.deregisterProjectChangeListener(projectChangeListener)
    }

    override fun filterCreated(filter: EventFilterDefinition) {
        if (createdProviders.containsKey(filter)) throw IllegalArgumentException("Duplicate filter registered: $filter")

        val newProvider = PredicateDrawnEventProvider(filter)
        createdProviders.put(filter, newProvider)
        drawnEventProviderManager.registeredProviders.add(newProvider)
    }

    override fun filterRemoved(filter: EventFilterDefinition) {
        createdProviders.remove(filter)?.let { drawnEventProviderManager.registeredProviders.remove(it) }
    }

}
