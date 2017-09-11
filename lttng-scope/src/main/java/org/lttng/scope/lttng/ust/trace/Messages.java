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

package org.lttng.scope.lttng.ust.core.trace;

/**
 * Message bundle for lttng2.kernel.core.trace
 */
@SuppressWarnings("javadoc")
public class Messages {

    static final String LttngUstTrace_DomainError = "Domain mismatch, the environment should be 'ust'.";
    static final String LttngUstTrace_MalformedTrace = "Buffer overflow exception, trace is malformed";
    static final String LttngUstTrace_TraceReadError = "Lttng UST trace read error";

    private Messages() {
    }
}
