/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lttng.ust.core.analysis.debuginfo.aspect;

import static org.lttng.scope.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.lttng.scope.jabberwocky.JabberwockyProjectManager;
import org.lttng.scope.lttng.ust.core.trace.LttngUstTrace;

import com.efficios.jabberwocky.lttng.ust.analysis.debuginfo.BinaryCallsite;
import com.efficios.jabberwocky.lttng.ust.analysis.debuginfo.UstDebugInfoAnalysis;
import com.efficios.jabberwocky.lttng.ust.analysis.debuginfo.UstDebugInfoAnalysisResults;
import com.efficios.jabberwocky.lttng.ust.analysis.debuginfo.UstDebugInfoLoadedBinaryFile;
import com.efficios.jabberwocky.project.TraceProject;

import ca.polymtl.dorsal.libdelorean.IStateSystemReader;

/**
 * Event aspect of UST traces that indicate the binary callsite (binary, symbol
 * and offset) from an IP (instruction pointer) context.
 *
 * Unlike the {@link UstDebugInfoSourceAspect}, this information should be
 * available even without debug information.
 *
 * @author Alexandre Montplaisir
 */
public class UstDebugInfoBinaryAspect implements ITmfEventAspect<BinaryCallsite> {

    /** Singleton instance */
    public static final UstDebugInfoBinaryAspect INSTANCE = new UstDebugInfoBinaryAspect();

    private UstDebugInfoBinaryAspect() {}

    @Override
    public String getName() {
        return nullToEmptyString(Messages.UstDebugInfoAnalysis_BinaryAspectName);
    }

    @Override
    public String getHelpText() {
        return nullToEmptyString(Messages.UstDebugInfoAnalysis_BinaryAspectHelpText);
    }

    @Override
    public @Nullable BinaryCallsite resolve(ITmfEvent event) {
        /* This aspect only supports UST traces */
        if (!(event.getTrace() instanceof LttngUstTrace)) {
            return null;
        }
        LttngUstTrace trace = (LttngUstTrace) event.getTrace();

        /* We need both the vpid and ip contexts */
        ITmfEventField vpidField = event.getContent().getField("context._vpid"); //$NON-NLS-1$
        ITmfEventField ipField = event.getContent().getField("context._ip"); //$NON-NLS-1$
        if (vpidField == null || ipField == null) {
            return null;
        }
        Long vpid = (Long) vpidField.getValue();
        Long ip = (Long) ipField.getValue();
        long ts = event.getTimestamp().toNanos();

        return getBinaryCallsite(trace, vpid.intValue(), ts, ip.longValue());
    }

    /**
     * Get the binary callsite (which means binary file and offset in this file)
     * corresponding to the given instruction pointer, for the given PID and
     * timetamp.
     *
     * @param trace
     *            The trace, from which we will get the debug info analysis
     * @param pid
     *            The PID for which we want the symbol
     * @param ts
     *            The timestamp of the query
     * @param ip
     *            The instruction pointer address
     * @return The {@link BinaryCallsite} object with the relevant information
     */
    public static @Nullable BinaryCallsite getBinaryCallsite(LttngUstTrace trace, int pid, long ts, long ip) {
        /*
         * First match the IP to the correct binary or library, by using the
         * UstDebugInfoAnalysis.
         */
        TraceProject project = trace.getJwProject();
        UstDebugInfoAnalysis analysis = UstDebugInfoAnalysis.instance();

        if (!analysis.canExecute(project)) {
            return null;
        }

        Object res = JabberwockyProjectManager.instance().getAnalysisResults(project, analysis);
        IStateSystemReader ss = (IStateSystemReader) res;
        UstDebugInfoAnalysisResults results = new UstDebugInfoAnalysisResults(ss);

        UstDebugInfoLoadedBinaryFile file = results.getMatchingFile(ts, pid, ip);
        if (file == null) {
            return null;
        }

        /* Apply the path prefix defined by the trace, if any */
        String fullPath = (trace.getSymbolProviderConfig().getActualRootDirPath() + file.getFilePath());

        long offset;
        if (file.isPic()) {
            offset = ip - file.getBaseAddress();
        } else {
            /*
             * In the case of the object being non-position-independent, we
             * must pass the actual 'ip' address directly to addr2line.
             */
            offset = ip;
        }

        return new BinaryCallsite(fullPath, file.getBuildId(), offset, file.isPic());
    }
}
