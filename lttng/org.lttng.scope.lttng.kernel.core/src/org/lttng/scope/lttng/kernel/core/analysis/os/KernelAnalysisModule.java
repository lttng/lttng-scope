/*******************************************************************************
 * Copyright (c) 2013, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Mathieu Rail - Provide the requirements of the analysis
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.analysis.os;

import static java.util.Objects.requireNonNull;
import static org.lttng.scope.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.lttng.kernel.core.analysis.os.internal.KernelStateProvider;
import org.lttng.scope.lttng.kernel.core.analysis.os.trace.DefaultEventLayout;
import org.lttng.scope.lttng.kernel.core.trace.IKernelTrace;
import org.lttng.scope.lttng.kernel.core.trace.layout.ILttngKernelEventLayout;

/**
 * State System Module for lttng kernel traces
 *
 * @author Geneviève Bastien
 */
public class KernelAnalysisModule extends TmfStateSystemAnalysisModule {

    /** The ID of this analysis module */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.kernel"; //$NON-NLS-1$

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        ITmfTrace trace = requireNonNull(getTrace());
        ILttngKernelEventLayout layout;

        if (trace instanceof IKernelTrace) {
            layout = ((IKernelTrace) trace).getKernelEventLayout();
        } else {
            /* Fall-back to the base LttngEventLayout */
            layout = DefaultEventLayout.getInstance();
        }

        return new KernelStateProvider(trace, layout);
    }

    @Override
    protected String getFullHelpText() {
        return nullToEmptyString(Messages.LttngKernelAnalysisModule_Help);
    }
}
