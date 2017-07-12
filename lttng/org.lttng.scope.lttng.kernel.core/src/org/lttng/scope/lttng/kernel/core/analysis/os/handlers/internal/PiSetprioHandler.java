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
import org.lttng.scope.lttng.kernel.core.trace.layout.ILttngKernelEventLayout;

import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue;
import com.efficios.jabberwocky.trace.event.ITraceEvent;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.TmfStateValue;

/**
 * Set Prio handler
 */
public class PiSetprioHandler extends KernelEventHandler {

    /**
     * Constructor
     * @param layout event layout
     */
    public PiSetprioHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITraceEvent event) throws AttributeNotFoundException {
        int cpu = event.getCpu();
        Long tid = requireNonNull(event.getField(getLayout().fieldTid(), IntegerValue.class)).getValue();
        Long prio = requireNonNull(event.getField(getLayout().fieldNewPrio(), IntegerValue.class)).getValue();

        String threadAttributeName = Attributes.buildThreadAttributeName(tid.intValue(), cpu);
        if (threadAttributeName == null) {
            return;
        }

        Integer updateThreadNode = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeThreads(ss), threadAttributeName);

        /* Set the current prio for the new process */
        int quark = ss.getQuarkRelativeAndAdd(updateThreadNode, Attributes.PRIO);
        ITmfStateValue value = TmfStateValue.newValueInt(prio.intValue());
        ss.modifyAttribute(event.getTimestamp(), value, quark);
    }
}
