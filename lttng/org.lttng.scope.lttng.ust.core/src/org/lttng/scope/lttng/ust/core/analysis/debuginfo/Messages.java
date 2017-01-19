/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lttng.ust.core.analysis.debuginfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle
 *
 * @author Alexandre Montplaisir
 * @noreference Messages class
 */
@NonNullByDefault({})
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String UstDebugInfoAnalysis_BinaryAspectName;
    public static String UstDebugInfoAnalysis_BinaryAspectHelpText;
    public static String UstDebugInfoAnalysis_FunctionAspectName;
    public static String UstDebugInfoAnalysis_FunctionAspectHelpText;
    public static String UstDebugInfoAnalysis_SourceAspectName;
    public static String UstDebugInfoAnalysis_SourceAspectHelpText;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
