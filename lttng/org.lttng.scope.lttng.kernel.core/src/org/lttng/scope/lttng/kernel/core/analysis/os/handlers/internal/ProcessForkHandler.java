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
import com.efficios.jabberwocky.trace.event.FieldValue.StringValue;
import com.efficios.jabberwocky.trace.event.ITraceEvent;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.TmfStateValue;

/**
 * Fork Handler
 */
public class ProcessForkHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public ProcessForkHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITraceEvent event) throws AttributeNotFoundException {
        int cpu = event.getCpu();
        String childProcessName = requireNonNull(event.getField(getLayout().fieldChildComm(), StringValue.class)).getValue();
        int parentTid = Long.valueOf(requireNonNull(event.getField(getLayout().fieldParentTid(), IntegerValue.class)).getValue()).intValue();
        int childTid = Long.valueOf(requireNonNull(event.getField(getLayout().fieldChildTid(), IntegerValue.class)).getValue()).intValue();

        String parentThreadAttributeName = Attributes.buildThreadAttributeName(parentTid, cpu);
        if (parentThreadAttributeName == null) {
            return;
        }

        String childThreadAttributeName = Attributes.buildThreadAttributeName(childTid, cpu);
        if (childThreadAttributeName == null) {
            return;
        }

        final int threadsNode = KernelEventHandlerUtils.getNodeThreads(ss);
        Integer parentTidNode = ss.getQuarkRelativeAndAdd(threadsNode, parentThreadAttributeName);
        Integer childTidNode = ss.getQuarkRelativeAndAdd(threadsNode, childThreadAttributeName);


        /* Assign the PPID to the new process */
        int quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.PPID);
        ITmfStateValue value = TmfStateValue.newValueInt(parentTid);
        long timestamp = event.getTimestamp();
        ss.modifyAttribute(timestamp, value, quark);

        /* Set the new process' exec_name */
        quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.EXEC_NAME);
        value = TmfStateValue.newValueString(childProcessName);
        ss.modifyAttribute(timestamp, value, quark);

        /*
         * Set the new process' status, it is initially in a blocked state. A
         * subsequent sched_wakeup_new will schedule it.
         */
        value = StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE;
        ss.modifyAttribute(timestamp, value, childTidNode);

        /* Set the process' syscall name, to be the same as the parent's */
        quark = ss.getQuarkRelativeAndAdd(parentTidNode, Attributes.SYSTEM_CALL);
        value = ss.queryOngoingState(quark);
        if (!value.isNull()) {
            quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.SYSTEM_CALL);
            ss.modifyAttribute(timestamp, value, quark);
        }

    }
}
