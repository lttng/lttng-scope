/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import com.efficios.jabberwocky.collection.TraceCollection;
import com.efficios.jabberwocky.project.ITraceProject;
import com.efficios.jabberwocky.project.ITraceProjectIterator;
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.trace.event.TraceEvent;
import com.google.common.io.MoreFiles;

class StubProject implements ITraceProject<TraceEvent, StubTrace>, AutoCloseable {

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

    @Override
    public Path getDirectory() {
        return fInnerProject.getDirectory();
    }

    @Override
    public long getEndTime() {
        return fInnerProject.getEndTime();
    }

    @Override
    public String getName() {
        return fInnerProject.getName();
    }

    @Override
    public long getStartTime() {
        return fInnerProject.getStartTime();
    }

    @Override
    public Collection getTraceCollections() {
        return fInnerProject.getTraceCollections();
    }

    @Override
    public ITraceProjectIterator<TraceEvent> iterator() {
        return fInnerProject.iterator();
    }

    @Override
    public void close() {
        try {
            MoreFiles.deleteRecursively(fProjectPath);
        } catch (IOException e) {
        }
    }

}
