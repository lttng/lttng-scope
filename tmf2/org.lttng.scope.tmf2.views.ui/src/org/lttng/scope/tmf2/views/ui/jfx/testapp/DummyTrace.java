package org.lttng.scope.tmf2.views.ui.jfx.testapp;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.lttng.scope.tmf2.views.core.TimeRange;

public class DummyTrace extends TmfTrace {

//    private static final long START_TIME = 100000;
//    private static final long END_TIME   = 200000;

    private static final long START_TIME = 1332170682440133097L;
    private static final long END_TIME   = 1332170692664579801L;

    public DummyTrace() {
        setTimeRange(TimeRange.of(START_TIME, END_TIME).toTmfTimeRange());
    }

    @Override
    public ITmfTimestamp getInitialRangeOffset() {
        return TmfTimestamp.fromNanos((long) ((END_TIME - START_TIME) * 0.1));
    }

    // ------------------------------------------------------------------------
    // Stuff we don't use
    // ------------------------------------------------------------------------

    @Override
    public @Nullable IStatus validate(@Nullable IProject project, @Nullable String path) {
        return null;
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
        return 0;
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
