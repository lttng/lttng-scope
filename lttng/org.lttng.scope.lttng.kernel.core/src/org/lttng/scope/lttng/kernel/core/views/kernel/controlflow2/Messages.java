/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lttng.kernel.core.views.kernel.controlflow2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Messages class
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("javadoc")
@NonNullByDefault({})
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String ControlFlowRenderProvider_State_WaitUnknown;
    public static String ControlFlowRenderProvider_State_WaitBlocked;
    public static String ControlFlowRenderProvider_State_WaitForCpu;
    public static String ControlFlowRenderProvider_State_UserMode;
    public static String ControlFlowRenderProvider_State_Syscall;
    public static String ControlFlowRenderProvider_State_Interrupted;
    public static String ControlFlowRenderProvider_State_Unknown;

    public static String ControlFlowSortingModes_ByTid;
    public static String ControlFlowSortingModes_ByThreadName;

    public static String ControlFlowFilterModes_InactiveEntries;

    public static String threadsProviderName;

    public static String arrowSeriesCPUs;

    public static String propertyNotAvailable;
    public static String propertyNameCpu;
    public static String propertyNameSyscall;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
