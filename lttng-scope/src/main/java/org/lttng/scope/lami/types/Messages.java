/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lami.types;

/**
 * Message bundle for the package
 */
@SuppressWarnings("javadoc")
public class Messages {

    static final String LamiBoolean_Yes = "Yes";
    static final String LamiBoolean_No = "No";

    static final String LamiData_Value = "Value";
    static final String LamiData_UnitBytes = "bytes";
    static final String LamiData_UnitBitsPerSecond = "bps";

    static final String LamiIRQ_SoftIRQ = "SoftIRQ";
    static final String LamiIRQ_HardwareIRQ = "IRQ";

    private Messages() {
    }
}
