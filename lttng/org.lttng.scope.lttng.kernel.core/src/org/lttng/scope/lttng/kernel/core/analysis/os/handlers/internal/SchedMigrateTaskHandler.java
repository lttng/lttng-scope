/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.analysis.os.StateValues;
import org.lttng.scope.lttng.kernel.core.trace.layout.ILttngKernelEventLayout;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.TmfStateValue;

/**
 * Handler for task migration events. Normally moves a (non-running) process
 * from one run queue to another.
 *
 * @author Alexandre Montplaisir
 */
public class SchedMigrateTaskHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            The event layout to use
     */
    public SchedMigrateTaskHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        Long tid = event.getContent().getFieldValue(Long.class, getLayout().fieldTid());
        Long destCpu = event.getContent().getFieldValue(Long.class, getLayout().fieldDestCpu());

        if (tid == null || destCpu == null) {
            return;
        }

        long t = event.getTimestamp().toNanos();

        String threadAttributeName = Attributes.buildThreadAttributeName(tid.intValue(), null);
        if (threadAttributeName == null) {
            /* Swapper threads do not get migrated */
            return;
        }
        int threadNode = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeThreads(ss), threadAttributeName);

        /*
         * Put the thread in the "wait for cpu" state. Some older versions of
         * the kernel/tracers may not have the corresponding sched_waking events
         * that also does so, so we can set it at the migrate, if applicable.
         */
        ITmfStateValue value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
        ss.modifyAttribute(t, value, threadNode);

        /* Update the thread's running queue to the new one indicated by the event */
        int quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.CURRENT_CPU_RQ);
        value = TmfStateValue.newValueInt(destCpu.intValue());
        ss.modifyAttribute(t, value, quark);
    }

}
