/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.ust.analysis.debuginfo;

import ca.polymtl.dorsal.libdelorean.IStateSystemWriter;
import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis;
import com.efficios.jabberwocky.collection.TraceCollection;
import com.efficios.jabberwocky.ctf.trace.CtfTrace;
import com.efficios.jabberwocky.ctf.trace.CtfTraceUtilsKt;
import com.efficios.jabberwocky.lttng.ust.trace.LttngUstTraceUtilsKt;
import com.efficios.jabberwocky.lttng.ust.trace.layout.ILttngUstEventLayout;
import com.efficios.jabberwocky.lttng.ust.trace.layout.LttngUst28EventLayout;
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.trace.Trace;
import com.efficios.jabberwocky.trace.event.TraceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Analysis to provide TMF Callsite information by mapping IP (instruction pointer) contexts to address/line numbers via
 * debug information.
 *
 * @author Alexandre Montplaisir
 */
public class UstDebugInfoAnalysis extends StateSystemAnalysis {

    private static final UstDebugInfoAnalysis INSTANCE = new UstDebugInfoAnalysis();

    public static UstDebugInfoAnalysis instance() {
        return INSTANCE;
    }

    private static final int VERSION = 5;

    private UstDebugInfoAnalysis() {
    }

    // ------------------------------------------------------------------------
    // IAnalysis
    // ------------------------------------------------------------------------

    @Override
    public boolean appliesTo(@NotNull TraceProject<?, ?> project) {
        /* Project should contain at least one UST trace */
        return project.getTraceCollections().stream()
                .flatMap(collection -> collection.getTraces().stream())
                .filter(trace -> trace instanceof CtfTrace).map(trace -> (CtfTrace) trace)
                .anyMatch(LttngUstTraceUtilsKt::isUstTrace);
    }

    @Override
    public boolean canExecute(@NotNull TraceProject<?, ?> project) {
        return (getExecutableTraces(project).count() >= 1);
    }

    /**
     * Return the traces from LTTng-UST >= 2.8 in the project
     */
    private static final Stream<CtfTrace> getExecutableTraces(TraceProject<?, ?> project) {
        return project.getTraceCollections().stream()
                .flatMap(collection -> collection.getTraces().stream())
                .filter(trace -> trace instanceof CtfTrace).map(trace -> (CtfTrace) trace)
                .filter(LttngUstTraceUtilsKt::isUstTrace)
                .filter(tracerIsLttng28OrAbove);
    }

    private static final Predicate<CtfTrace> tracerIsLttng28OrAbove = trace -> {
        String tracerName = CtfTraceUtilsKt.getTracerName(trace);
        Integer majorVersion = CtfTraceUtilsKt.getTracerMajorVersion(trace);
        Integer minorVersion = CtfTraceUtilsKt.getTracerMinorVersion(trace);

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
    protected TraceCollection<?, ?> filterTraces(TraceProject<?, ?> project) {
        return new TraceCollection(getExecutableTraces(project).collect(Collectors.toList()));
    }

    @Override
    protected Object[] trackedState() {
        return new Object[] { new UstDebugInfoAnalysisStateProvider()};
    }

    @Override
    protected void handleEvent(@NotNull IStateSystemWriter ss, @NotNull TraceEvent event, @Nullable Object[] trackedState) {
        Trace trace = event.getTrace();
        if (!(trace instanceof CtfTrace) || !LttngUstTraceUtilsKt.isUstTrace((CtfTrace) trace)) {
            return;
        }
        CtfTrace ustTrace = (CtfTrace) trace;
        ILttngUstEventLayout layout = LttngUstTraceUtilsKt.getUstEventLayout(ustTrace);
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

}
