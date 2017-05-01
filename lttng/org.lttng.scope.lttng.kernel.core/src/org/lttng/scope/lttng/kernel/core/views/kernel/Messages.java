/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.kernel.core.views.kernel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Messages class
 *
 * @author Alexandre Montplaisir
 * @noreference Message class
 */
@SuppressWarnings("javadoc")
@NonNullByDefault({})
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String noState;

    public static String threadStateUnknown;
    public static String threadStateWaitUnknown;
    public static String threadStateWaitBlocked;
    public static String threadStateWaitForCpu;
    public static String threadSateUsermode;
    public static String threadStateSyscall;
    public static String threadStateInterrupted;

    public static String cpuStateUnknown;
    public static String cpuStateIdle;
    public static String cpuStateIrqActive;
    public static String cpuStateSoftIrqActive;
    public static String cpuStateSoftIrqRaised;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
