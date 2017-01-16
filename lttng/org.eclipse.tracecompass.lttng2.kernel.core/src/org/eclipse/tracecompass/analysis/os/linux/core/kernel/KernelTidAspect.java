/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.kernel;

import static org.lttng.jabberwocky.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * This aspect finds the ID of the thread running from this event using the
 * {@link KernelAnalysisModule}.
 *
 * @author Geneviève Bastien
 */
public final class KernelTidAspect implements ITmfEventAspect<Integer> {

    /** The singleton instance */
    public static final KernelTidAspect INSTANCE = new KernelTidAspect();

    private static final IProgressMonitor NULL_MONITOR = new NullProgressMonitor();

    private KernelTidAspect() {
    }

    @Override
    public String getName() {
        return nullToEmptyString(Messages.AspectName_Tid);
    }

    @Override
    public String getHelpText() {
        return nullToEmptyString(Messages.AspectHelpText_Tid);
    }

    @Override
    public @Nullable Integer resolve(ITmfEvent event) {
        return resolve(event, false, NULL_MONITOR);
    }

    @Override
    public @Nullable Integer resolve(@NonNull ITmfEvent event, boolean block, final IProgressMonitor monitor) {
        /* Find the CPU this event is run on */
        Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(),
                TmfCpuAspect.class, event);
        if (cpu == null) {
            return null;
        }

        /* Find the analysis module for the trace */
        KernelAnalysisModule analysis = TmfTraceUtils.getAnalysisModuleOfClass(event.getTrace(),
                KernelAnalysisModule.class, KernelAnalysisModule.ID);
        if (analysis == null) {
            return null;
        }
        long ts = event.getTimestamp().toNanos();
        while (block && !analysis.isQueryable(ts) && !monitor.isCanceled()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return KernelThreadInformationProvider.getThreadOnCpu(analysis, cpu, ts);
    }

}
