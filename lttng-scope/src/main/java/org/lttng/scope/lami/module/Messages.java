/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lami.module;

/**
 * Message bundle for the package
 */
@SuppressWarnings("javadoc")
public class Messages {

    static final String LamiAnalysis_DefaultDynamicTableName = "Dynamic Table";

    static final String LamiAnalysis_MainTaskName = "Invoking external analysis script";
    static final String LamiAnalysis_NoResults = "No results were returned.";
    static final String LamiAnalysis_ErrorDuringExecution = "Error during execution of the script.";
    static final String LamiAnalysis_ErrorNoOutput = "(No output)";
    static final String LamiAnalysis_ExecutionInterrupted = "Execution was interrupted.";

    static final String LamiAnalysis_ExtendedTableNamePrefix = "Extended";

    private Messages() {
    }
}
