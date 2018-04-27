/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application.actions

import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.task.JabberwockyTask
import javafx.application.Platform
import javafx.scene.Node
import org.lttng.scope.common.LatestTaskExecutor
import org.lttng.scope.common.jfx.JfxUtils
import org.lttng.scope.views.context.ViewGroupContextManager

private val projectOpenExecutor = LatestTaskExecutor()

fun createNewProjectAction(refNode: Node): TraceProject<*, *>? {
    return with(ProjectSetupDialog(refNode, null)) {
        setOnShowing {
            Platform.runLater {
                JfxUtils.centerDialogOnScreen(this, refNode)
                /* Automatically open the "select trace" dialog since we are creating a project from scratch. */
                addTraceAction()
            }
        }
        showAndWait().orElse(null)
    }
}

fun setActiveProject(project: TraceProject<*, *>) {
    /*
     * Switch to a "real" (not the null/empty) project in a separate Task, so
     * that indexing progress is reported.
     */
    JabberwockyTask<Unit>("Analyzing project ${project.name}") {
        // Only one view context for now
        val viewCtx = ViewGroupContextManager.getCurrent()
        viewCtx.switchProject(project)
    }
            .let { projectOpenExecutor.schedule(it) }
}
