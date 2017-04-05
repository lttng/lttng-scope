/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;

class StubTrace extends TmfTrace {

    public static final long FULL_TRACE_START_TIME = 100000L;
    public static final long FULL_TRACE_END_TIME = 200000L;
    public static final long INITIAL_RANGE_OFFSET = 50000L;

    @Override
    public ITmfTimestamp getStartTime() {
        return TmfTimestamp.fromNanos(FULL_TRACE_START_TIME);
    }

    @Override
    public ITmfTimestamp getEndTime() {
        return TmfTimestamp.fromNanos(FULL_TRACE_END_TIME);
    }

    @Override
    public ITmfTimestamp getInitialRangeOffset() {
        return TmfTimestamp.fromNanos(INITIAL_RANGE_OFFSET);
    }

    // ------------------------------------------------------------------------
    // Useless stuff
    // ------------------------------------------------------------------------

    @Override
    public IStatus validate(@Nullable IProject project, @Nullable String path) {
        return Status.OK_STATUS;
    }

    @Override
    public @Nullable IResource getResource() {
        return null;
    }

    @Override
    public @Nullable ITmfLocation getCurrentLocation() {
        return null;
    }

    @Override
    public double getLocationRatio(@Nullable ITmfLocation location) {
        return 0.0;
    }

    @Override
    public @Nullable ITmfContext seekEvent(@Nullable ITmfLocation location) {
        return null;
    }

    @Override
    public @Nullable ITmfContext seekEvent(double ratio) {
        return null;
    }

    @Override
    public @Nullable ITmfEvent parseEvent(@Nullable ITmfContext context) {
        return null;
    }

}
