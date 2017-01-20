/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.lttng.scope.lttng.kernel.core.trace;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.lttng.kernel.core.trace.layout.ILttngKernelEventLayout;

/**
 * Trace type that represents a Linux kernel trace.
 *
 * Any trace implementing the interface should be able to run the different
 * Linux kernel analyses in this plugin.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public interface IKernelTrace extends ITmfTrace {

    /**
     * Get the event layout of this trace. Many known concepts from the Linux
     * kernel may be exported under different names, depending on the tracer.
     *
     * @return The event layout
     */
    ILttngKernelEventLayout getKernelEventLayout();

}
