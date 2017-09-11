///*******************************************************************************
// * Copyright (c) 2015 École Polytechnique de Montréal
// *
// * All rights reserved. This program and the accompanying materials are
// * made available under the terms of the Eclipse Public License v1.0 which
// * accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *   Geneviève Bastien - Initial API and implementation
// *******************************************************************************/
//
//package org.lttng.scope.lttng.kernel.core.event.aspect;
//
//import static org.lttng.scope.common.core.NonNullUtils.nullToEmptyString;
//
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.NullProgressMonitor;
//import org.eclipse.jdt.annotation.NonNull;
//import org.eclipse.jdt.annotation.Nullable;
//import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
//import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
//import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
//import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
//import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
//import org.lttng.scope.jabberwocky.JabberwockyProjectManager;
//import org.lttng.scope.lttng.kernel.core.trace.LttngKernelTrace;
//
//import com.efficios.jabberwocky.lttng.kernel.analysis.os.KernelAnalysis;
//import com.efficios.jabberwocky.lttng.kernel.analysis.os.KernelThreadInformationProvider;
//import com.efficios.jabberwocky.project.TraceProject;
//
//import ca.polymtl.dorsal.libdelorean.IStateSystemReader;
//
///**
// * This aspect finds the ID of the thread running from this event using the
// * {@link KernelAnalysis}.
// *
// * @author Geneviève Bastien
// */
//public final class KernelTidAspect implements ITmfEventAspect<Integer> {
//
//    /** The singleton instance */
//    public static final KernelTidAspect INSTANCE = new KernelTidAspect();
//
//    private static final IProgressMonitor NULL_MONITOR = new NullProgressMonitor();
//
//    private KernelTidAspect() {
//    }
//
//    @Override
//    public String getName() {
//        return nullToEmptyString(Messages.KernelTidAspect_Name);
//    }
//
//    @Override
//    public String getHelpText() {
//        return nullToEmptyString(Messages.KernelTidAspect_HelpText);
//    }
//
//    @Override
//    public @Nullable Integer resolve(ITmfEvent event) {
//        return resolve(event, false, NULL_MONITOR);
//    }
//
//    @Override
//    public @Nullable Integer resolve(@NonNull ITmfEvent event, boolean block, final IProgressMonitor monitor) {
//        /* Find the CPU this event is run on */
//        Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(),
//                TmfCpuAspect.class, event);
//        if (cpu == null) {
//            return null;
//        }
//
//        /* Find the analysis module for the trace */
//        final @NonNull ITmfTrace trace = event.getTrace();
//        if (!(trace instanceof LttngKernelTrace)) {
//            return null;
//        }
//        LttngKernelTrace kTrace = (LttngKernelTrace) trace;
//        TraceProject<?, ?> project = kTrace.getJwProject();
//        KernelAnalysis analysis = KernelAnalysis.instance();
//        JabberwockyProjectManager mgr = JabberwockyProjectManager.instance();
//        IStateSystemReader ss = (IStateSystemReader) mgr.getAnalysisResults(project, analysis);
//
//        long ts = event.getTimestamp().toNanos();
//        return KernelThreadInformationProvider.getThreadOnCpu(ss, cpu, ts);
//    }
//
//}
