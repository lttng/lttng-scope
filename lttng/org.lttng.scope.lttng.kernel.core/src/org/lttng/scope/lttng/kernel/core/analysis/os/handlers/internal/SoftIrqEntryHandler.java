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

import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.lttng.scope.lttng.kernel.core.analysis.os.StateValues;
import org.lttng.scope.lttng.kernel.core.trace.layout.ILttngKernelEventLayout;

/**
 * Soft Irq Entry handler
 */
public class SoftIrqEntryHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public SoftIrqEntryHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        Integer cpu = KernelEventHandlerUtils.getCpu(event);
        if (cpu == null) {
            return;
        }

        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        Integer softIrqId = ((Long) event.getContent().getField(getLayout().fieldVec()).getValue()).intValue();
        int currentCPUNode = KernelEventHandlerUtils.getCurrentCPUNode(cpu, ss);
        int currentThreadNode = KernelEventHandlerUtils.getCurrentThreadNode(cpu, ss);

        /*
         * Mark this SoftIRQ as active in the resource tree.
         */
        int quark = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeSoftIRQs(cpu, ss), softIrqId.toString());
        ITmfStateValue value = StateValues.CPU_STATUS_SOFTIRQ_VALUE;
        ss.modifyAttribute(timestamp, value, quark);

        /* Change the status of the running process to interrupted */
        value = StateValues.PROCESS_STATUS_INTERRUPTED_VALUE;
        ss.modifyAttribute(timestamp, value, currentThreadNode);

        /* Change the status of the CPU to interrupted */
        value = StateValues.CPU_STATUS_SOFTIRQ_VALUE;
        ss.modifyAttribute(timestamp, value, currentCPUNode);
    }
}
