/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.views.timegraph;

/**
 * Messages class
 *
 * @author Alexandre Montplaisir
 * @noreference Message class
 */
@SuppressWarnings("javadoc")
public class Messages {

    public static final String noState = "None";

    public static final String threadStateUnknown = "Unknown Thread State";
    public static final String threadStateWaitUnknown = "Wait Unknown";
    public static final String threadStateWaitBlocked = "Waiting Blocked";
    public static final String threadStateWaitForCpu = "Waiting For CPU";
    public static final String threadSateUsermode = "Running, usermode";
    public static final String threadStateSyscall = "Running, syscall";
    public static final String threadStateInterrupted = "Interrupted";

    public static final String cpuStateUnknown = "Unknown CPU State";
    public static final String cpuStateIdle = "Idle";
    public static final String cpuStateIrqActive = "Executing IRQ";
    public static final String cpuStateSoftIrqActive = "Executing Soft IRQ";
    public static final String cpuStateSoftIrqRaised = "Soft IRQ Raised";

}
