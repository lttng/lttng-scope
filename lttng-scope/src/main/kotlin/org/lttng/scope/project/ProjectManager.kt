/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.project

import com.efficios.jabberwocky.project.TraceProject
import org.lttng.scope.project.filter.EventFilterDefinition

/**
 * Application-side manager that keeps track of active trace projects, and
 * the viewer-side state that we want to associate to them.
 */
object ProjectManager {

    private val projectStates = mutableMapOf<TraceProject<*, *>, ProjectState>()

    @Synchronized
    fun getProjectState(project: TraceProject<*, *>): ProjectState {
        var state = projectStates[project]
        if (state == null) {
            state = ProjectState(project)
            projectStates.put(project, state)
        }
        return state
    }

    /**
     * Clear the "cache" for one given project. Usually should be called when said
     * project is destroyed.
     *
     * @param project The project to dispose of
     */
    @Synchronized
    fun dispose(project: TraceProject<*, *>) {
        projectStates.remove(project);
    }

}
