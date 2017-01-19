/*******************************************************************************
 * Copyright (c) 2015 Keba AG
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Christian Mansky - Initial implementation
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.event.aspect;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.lttng.scope.common.core.NonNullUtils;
import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.analysis.os.KernelAnalysisModule;
import org.lttng.scope.lttng.kernel.core.analysis.os.KernelTidAspect;

/**
 * This aspect finds the priority of the thread running from this event using
 * the {@link KernelAnalysisModule}.
 *
 * @author Christian Mansky
 */
public final class ThreadPriorityAspect implements ITmfEventAspect<Integer> {

    /** The singleton instance */
    public static final ThreadPriorityAspect INSTANCE = new ThreadPriorityAspect();

    private ThreadPriorityAspect() {
    }

    @Override
    public final String getName() {
        return NonNullUtils.nullToEmptyString(Messages.AspectName_Prio);
    }

    @Override
    public final String getHelpText() {
        return NonNullUtils.nullToEmptyString(Messages.AspectHelpText_Prio);
    }

    @Override
    public @Nullable Integer resolve(ITmfEvent event) {
        final @NonNull ITmfTrace trace = event.getTrace();
        KernelAnalysisModule kernelAnalysis = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class, KernelAnalysisModule.ID);
        if (kernelAnalysis == null) {
            return null;
        }

        ITmfStateSystem ss = kernelAnalysis.getStateSystem();
        if (ss == null) {
            return null;
        }

        Integer tid = KernelTidAspect.INSTANCE.resolve(event);
        if (tid == null) {
            return null;
        }

        final long ts = event.getTimestamp().getValue();
        Integer execPrio = null;
        try {
            Integer cpu = 0;
            if (tid == 0) {
                /* Find the CPU this event is run on */
                cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(trace, TmfCpuAspect.class, event);
            }
            int execPrioQuark = ss.getQuarkAbsolute(Attributes.THREADS, Attributes.buildThreadAttributeName(tid, cpu), Attributes.PRIO);
            ITmfStateInterval interval = ss.querySingleState(ts, execPrioQuark);
            ITmfStateValue prioValue = interval.getStateValue();
            /* We know the prio must be an Integer */
            execPrio = prioValue.unboxInt();
        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
        }
        return execPrio;
    }
}
