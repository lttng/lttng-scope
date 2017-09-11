/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.trace;

/**
 * Message bundle
 */
@SuppressWarnings("javadoc")
public class Messages {

    static final String LttngKernelTrace_DomainError = "Domain mismatch, the environment should be 'kernel'.";
    static final String LttngKernelTrace_MalformedTrace = "Buffer overflow exception, trace is malformed";
    static final String LttngKernelTrace_TraceReadError = "Lttng trace read error";

    private Messages() {
    }
}
