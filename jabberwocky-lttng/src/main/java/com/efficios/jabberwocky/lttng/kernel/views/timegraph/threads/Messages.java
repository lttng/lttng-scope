/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.lttng.kernel.views.timegraph.threads;

/**
 * Messages class
 *
 * @author Alexandre Montplaisir
 * @noreference Message class
 */
@SuppressWarnings("javadoc")
public class Messages {

    public static String ControlFlowSortingModes_ByTid = "Sort by TID";
    public static String ControlFlowSortingModes_ByThreadName = "Sort by Thread Name";

    public static String ControlFlowFilterModes_InactiveEntries = "Filter inactive entries";

    public static String threadsProviderName = "Threads";

    public static String arrowSeriesCPUs = "CPUs";

    public static String propertyNotAvailable = "N/A";
    public static String propertyNameCpu = "CPU";
    public static String propertyNameSyscall = "Syscall";

}
