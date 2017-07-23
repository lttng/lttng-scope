/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.ust.core.analysis.debuginfo;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis;
import com.efficios.jabberwocky.collection.ITraceCollection;
import com.efficios.jabberwocky.collection.TraceCollection;
import com.efficios.jabberwocky.ctf.trace.CtfTrace;
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent;
import com.efficios.jabberwocky.lttng.ust.trace.LttngUstTrace;
import com.efficios.jabberwocky.lttng.ust.trace.layout.ILttngUstEventLayout;
import com.efficios.jabberwocky.lttng.ust.trace.layout.LttngUst28EventLayout;
import com.efficios.jabberwocky.project.ITraceProject;
import com.efficios.jabberwocky.trace.ITrace;
import com.efficios.jabberwocky.trace.event.ITraceEvent;
import com.google.common.primitives.Ints;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;

/**
 * Analysis to provide TMF Callsite information by mapping IP (instruction
 * pointer) contexts to address/line numbers via debug information.
 *
 * @author Alexandre Montplaisir
 */
public class UstDebugInfoAnalysis extends StateSystemAnalysis {

    private static final UstDebugInfoAnalysis INSTANCE = new UstDebugInfoAnalysis();

    public static UstDebugInfoAnalysis instance() {
        return INSTANCE;
    }

    private static final int VERSION = 5;

    private UstDebugInfoAnalysis() {}

    // ------------------------------------------------------------------------
    // IAnalysis
    // ------------------------------------------------------------------------

    @Override
    public boolean appliesTo(ITraceProject<?, ?> project) {
        /* Project should contain at least one UST trace */
        return project.getTraceCollections().stream()
                .flatMap(collection -> collection.getTraces().stream())
                .anyMatch(trace -> trace instanceof LttngUstTrace);
    }

    @Override
    public boolean canExecute(ITraceProject<?, ?> project) {
        return (getExecutableTraces(project).count() >= 1);
    }

    /** Return the traces from LTTng-UST >= 2.8 in the project */
    private static final Stream<LttngUstTrace> getExecutableTraces(ITraceProject<?, ?> project) {
        return project.getTraceCollections().stream()
                .flatMap(collection -> collection.getTraces().stream())
                .filter(trace -> trace instanceof LttngUstTrace).map(trace -> (LttngUstTrace) trace)
                .filter(tracerIsLttng28OrAbove);
    }

    private static final Predicate<LttngUstTrace> tracerIsLttng28OrAbove = trace -> {
        String tracerName = getTracerName(trace);
        Integer majorVersion = getTracerMajorVersion(trace);
        Integer minorVersion = getTracerMinorVersion(trace);

        if (tracerName == null || majorVersion == null || minorVersion == null) {
            return false;
        }

        /* ... taken with UST >= 2.8 ... */
        if (!tracerName.equals("lttng-ust")) { //$NON-NLS-1$
            return false;
        }
        if (majorVersion < 2) {
            return false;
        }
        if (majorVersion == 2 && minorVersion < 8) {
            return false;
        }
        return true;
    };

    // ------------------------------------------------------------------------
    // StateSystemAnalysis
    // ------------------------------------------------------------------------

    @Override
    public int getProviderVersion() {
        return VERSION;
    }

    @Override
    protected ITraceCollection<?, ?> filterTraces(ITraceProject<?, ?> project) {
        return new TraceCollection<>(getExecutableTraces(project).collect(Collectors.toList()));
    }

    @Override
    protected Object @NonNull [] trackedState() {
        return new Object[] { new UstDebugInfoAnalysisStateProvider() };
    }

    @Override
    protected void handleEvent(ITmfStateSystemBuilder ss, ITraceEvent event, Object @Nullable [] trackedState) {
        ITrace trace = event.getTrace();
        if (!(trace instanceof LttngUstTrace)) {
            return;
        }
        LttngUstTrace ustTrace = (LttngUstTrace) trace;
        ILttngUstEventLayout layout = ustTrace.getUstEventLayout();
        if (!(layout instanceof LttngUst28EventLayout)) {
            /* We need at least LTTng-UST 2.8 for this analysis */
            return;
        }
        LttngUst28EventLayout layout28 = (LttngUst28EventLayout) layout;
        UstDebugInfoAnalysisDefinitions defs = UstDebugInfoAnalysisDefinitions.getDefsFromLayout(layout28);

        requireNonNull(trackedState);
        UstDebugInfoAnalysisStateProvider stateProvider = (UstDebugInfoAnalysisStateProvider) trackedState[0];

        /* Actual state changes are handled by the stateProvider object */
        stateProvider.eventHandle(defs, ss, event);
    }

    // ------------------------------------------------------------------------
    // CtfUtils (move to Kotlin extension methods?)
    // ------------------------------------------------------------------------

    /**
     * Convenience method to get the tracer name from the trace's metadata. The
     * leading and trailing "" will be stripped from the returned string.
     *
     * @param trace
     *            The trace to query
     * @return The tracer's name, or null if it is not defined in the metadata.
     */
    private static @Nullable String getTracerName(CtfTrace<CtfTraceEvent> trace) {
        String str = trace.getEnvironment().get("tracer_name"); //$NON-NLS-1$
        if (str == null) {
            return null;
        }
        /* Remove the "" at the start and end of the string */
        return str.replaceAll("^\"|\"$", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Convenience method to get the tracer's major version from the trace
     * metadata.
     *
     * @param trace
     *            The trace to query
     * @return The tracer's major version, or null if it is not defined.
     */
    private static @Nullable Integer getTracerMajorVersion(CtfTrace<CtfTraceEvent> trace) {
        String str = trace.getEnvironment().get("tracer_major"); //$NON-NLS-1$
        if (str == null) {
            return null;
        }
        return Ints.tryParse(str);
    }

    /**
     * Convenience method to get the tracer's minor version from the trace
     * metadata.
     *
     * @param trace
     *            The trace to query
     * @return The tracer's minor version, or null if it is not defined.
     */
    private static @Nullable Integer getTracerMinorVersion(CtfTrace<CtfTraceEvent> trace) {
        String str = trace.getEnvironment().get("tracer_minor"); //$NON-NLS-1$
        if (str == null) {
            return null;
        }
        return Ints.tryParse(str);
    }

}
