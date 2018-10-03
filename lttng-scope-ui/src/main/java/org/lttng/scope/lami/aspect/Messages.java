/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lami.aspect;

/**
 * Message bundle for the package
 *
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages {

    static final String LamiAspect_Name = "name";
    static final String LamiAspect_Type = "type";

    static final String LamiAspect_TimeRangeBegin = "begin";
    static final String LamiAspect_TimeRangeDuration = "duration";
    static final String LamiAspect_TimeRangeEnd = "end";

    static final String LamiIRQTypeAspect_HardwareIRQ = "Hard";
    static final String LamiIRQTypeAspect_SoftIRQ = "Soft";

    private Messages() {
    }
}
