/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common.tests

import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.trace.event.TraceEvent
import com.google.common.io.MoreFiles
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class StubProject(trace: StubTrace) : AutoCloseable {

    private val projectPath: Path
    val traceProject: TraceProject<TraceEvent, StubTrace>

    init {
        try {
            projectPath = Files.createTempDirectory("stub-project");
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
        traceProject = TraceProject.ofSingleTrace("stub-project", projectPath, trace)
    }

    override fun close() {
        try {
            MoreFiles.deleteRecursively(projectPath);
        } catch (e: IOException) {
        }
    }

}
