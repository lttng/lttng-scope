/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph;

import com.efficios.jabberwocky.collection.TraceCollection;
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.trace.event.TraceEvent;
import com.google.common.io.MoreFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

class StubProject implements AutoCloseable {

    private final Path fProjectPath;
    private final TraceProject<TraceEvent, StubTrace> fInnerProject;

    public StubProject(StubTrace trace) {
        TraceCollection<TraceEvent, StubTrace> coll = new TraceCollection<>(Collections.singleton(trace));

        try {
            fProjectPath = Files.createTempDirectory("stub-proj");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        fInnerProject = new TraceProject<>("stub-project", fProjectPath, Collections.singleton(coll));
    }

    public TraceProject<TraceEvent, StubTrace> getTraceProject() {
        return fInnerProject;
    }

    @Override
    public void close() {
        try {
            MoreFiles.deleteRecursively(fProjectPath);
        } catch (IOException e) {
        }
    }

}
