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

import org.lttng.scope.lttng.kernel.core.trace.layout.ILttngKernelEventLayout;

import com.efficios.jabberwocky.trace.event.ITraceEvent;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;

/**
 * Base class for all kernel event handlers.
 */
public abstract class KernelEventHandler {

    private final ILttngKernelEventLayout fLayout;

    /**
     * Constructor
     *
     * @param layout
     *            the analysis layout
     */
    public KernelEventHandler(ILttngKernelEventLayout layout) {
        fLayout = layout;
    }

    /**
     * Get the analysis layout
     *
     * @return the analysis layout
     */
    protected ILttngKernelEventLayout getLayout() {
        return fLayout;
    }

    /**
     * Handle a specific kernel event.
     *
     * @param ss
     *            the state system to write to
     * @param event
     *            the event
     * @throws AttributeNotFoundException
     *             if the attribute is not yet create
     */
    public abstract void handleEvent(ITmfStateSystemBuilder ss, ITraceEvent event) throws AttributeNotFoundException;

}
