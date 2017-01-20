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
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.lttng.scope.lttng.kernel.core.analysis.os.Attributes;
import org.lttng.scope.lttng.kernel.core.trace.layout.ILttngKernelEventLayout;

/**
 * Process free event handler
 */
public class ProcessFreeHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public ProcessFreeHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {

        Integer cpu = KernelEventHandlerUtils.getCpu(event);
        Integer tid = ((Long) event.getContent().getField(getLayout().fieldTid()).getValue()).intValue();

        String threadAttributeName = Attributes.buildThreadAttributeName(tid, cpu);
        if (threadAttributeName == null) {
            return;
        }

        /*
         * Remove the process and all its sub-attributes from the current state
         */
        int quark = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeThreads(ss), threadAttributeName);
        ss.removeAttribute(KernelEventHandlerUtils.getTimestamp(event), quark);
    }
}
