/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Improved validation
 ******************************************************************************/

package org.lttng.scope.lttng.kernel.core.trace;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.tmf.core.event.CtfTmfEventType;
import org.eclipse.tracecompass.ctf.tmf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.ctf.tmf.core.trace.CtfTraceValidationStatus;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.lttng.scope.lttng.kernel.core.activator.internal.Activator;
import org.lttng.scope.lttng.kernel.core.event.aspect.KernelTidAspect;
import org.lttng.scope.lttng.kernel.core.event.aspect.ThreadPriorityAspect;

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent;
import com.efficios.jabberwocky.trace.Trace;
import com.google.common.collect.ImmutableSet;

/**
 * This is the specification of CtfTmfTrace for use with LTTng 2.x kernel
 * traces.
 *
 * @author Alexandre Montplaisir
 */
public class LttngKernelTrace extends CtfTmfTrace {


    /**
     * Event aspects available for all Lttng Kernel traces
     */
    private static final @NonNull Collection<ITmfEventAspect<?>> LTTNG_KERNEL_ASPECTS;

    static {
        ImmutableSet.Builder<ITmfEventAspect<?>> builder = ImmutableSet.builder();
        builder.addAll(CtfTmfTrace.CTF_ASPECTS);
        builder.add(KernelTidAspect.INSTANCE);
        builder.add(ThreadPriorityAspect.INSTANCE);
        LTTNG_KERNEL_ASPECTS = builder.build();
    }

    /**
     * CTF metadata identifies trace type and tracer version pretty well, we are
     * quite confident in the inferred trace type.
     */
    private static final int CONFIDENCE = 100;

    /**
     * Default constructor
     */
    public LttngKernelTrace() {
        super();
    }

    @Override
    public void initTrace(IResource resource, String path,
            Class<? extends ITmfEvent> eventType) throws TmfTraceException {
        super.initTrace(resource, path, eventType);
    }

    @Override
    protected Trace<CtfTraceEvent> getJwTrace(Path tracePath) {
        return new com.efficios.jabberwocky.lttng.kernel.trace.LttngKernelTrace(tracePath);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation sets the confidence to 100 if the trace is a valid
     * CTF trace in the "kernel" domain.
     */
    @Override
    public IStatus validate(final IProject project, final String path) {
        IStatus status = super.validate(project, path);
        if (status instanceof CtfTraceValidationStatus) {
            Map<String, String> environment = ((CtfTraceValidationStatus) status).getEnvironment();
            /* Make sure the domain is "kernel" in the trace's env vars */
            String domain = environment.get("domain"); //$NON-NLS-1$
            if (domain == null || !domain.equals("\"kernel\"")) { //$NON-NLS-1$
                return new Status(IStatus.ERROR, Activator.instance().getPluginId(), Messages.LttngKernelTrace_DomainError);
            }
            return new TraceValidationStatus(CONFIDENCE, Activator.instance().getPluginId());
        }
        return status;
    }

    @Override
    public Iterable<ITmfEventAspect<?>> getEventAspects() {
         return LTTNG_KERNEL_ASPECTS;
    }

    /*
     * Needs explicit @NonNull generic type annotation. Can be removed once this
     * class becomes @NonNullByDefault.
     */
    @Override
    public @NonNull Set<@NonNull CtfTmfEventType> getContainedEventTypes() {
        return super.getContainedEventTypes();
    }

}
