/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lttng.ust.core.analysis.debuginfo.aspect;

/**
 * Message bundle
 */
@SuppressWarnings("javadoc")
public class Messages {

    static final String UstDebugInfoAnalysis_BinaryAspectName = "Binary Location";
    static final String UstDebugInfoAnalysis_BinaryAspectHelpText = "The call site of this event in the binary file";
    static final String UstDebugInfoAnalysis_FunctionAspectName = "Function Location";
    static final String UstDebugInfoAnalysis_FunctionAspectHelpText = "The call site location relative to the function/symbol";
    static final String UstDebugInfoAnalysis_SourceAspectName = "Source Location";
    static final String UstDebugInfoAnalysis_SourceAspectHelpText = "The call site of this event in the source code";

    private Messages() {
    }
}
