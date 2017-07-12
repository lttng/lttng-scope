/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.analysis.os.handlers.internal;

import static java.util.Objects.requireNonNull;

import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.analysis.os.StateValues;
import org.lttng.scope.lttng.kernel.core.trace.layout.ILttngKernelEventLayout;

import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue;
import com.efficios.jabberwocky.trace.event.ITraceEvent;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.TmfStateValue;

/**
 * Waking/wakeup handler.
 *
 * "sched_waking" and "sched_wakeup" tracepoints contain the same fields, and
 * apply the same state transitions in our model, so they can both use this
 * handler.
 */
public class SchedWakeupHandler extends KernelEventHandler {

    /**
     * Constructor
     * @param layout event layout
     */
    public SchedWakeupHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITraceEvent event) throws AttributeNotFoundException {
        int cpu = event.getCpu();
        final Long tid = requireNonNull(event.getField(getLayout().fieldTid(), IntegerValue.class)).getValue();
        final Long prio = requireNonNull(event.getField(getLayout().fieldPrio(), IntegerValue.class)).getValue();
        Long targetCpu = requireNonNull(event.getField(getLayout().fieldTargetCpu(), IntegerValue.class)).getValue();

        String threadAttributeName = Attributes.buildThreadAttributeName(tid.intValue(), cpu);
        if (threadAttributeName == null) {
            return;
        }

        final int threadNode = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeThreads(ss), threadAttributeName);

        /*
         * The process indicated in the event's payload is now ready to run.
         * Assign it to the "wait for cpu" state, but only if it was not already
         * running.
         */
        int status = ss.queryOngoingState(threadNode).unboxInt();
        ITmfStateValue value = null;
        long timestamp = event.getCpu();
        if (status != StateValues.PROCESS_STATUS_RUN_SYSCALL &&
                status != StateValues.PROCESS_STATUS_RUN_USERMODE) {
            value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
            ss.modifyAttribute(timestamp, value, threadNode);
        }

        /* Set the thread's target run queue */
        int quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.CURRENT_CPU_RQ);
        value = TmfStateValue.newValueInt(targetCpu.intValue());
        ss.modifyAttribute(timestamp, value, quark);

        /*
         * When a user changes a threads prio (e.g. with pthread_setschedparam),
         * it shows in ftrace with a sched_wakeup.
         */
        quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.PRIO);
        value = TmfStateValue.newValueInt(prio.intValue());
        ss.modifyAttribute(timestamp, value, quark);
    }
}
