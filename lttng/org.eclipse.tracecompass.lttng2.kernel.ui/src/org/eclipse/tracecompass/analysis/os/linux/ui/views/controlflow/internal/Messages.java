/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.views.controlflow.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the package
 *
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String ControlFlowView_NextEventActionName;
    public static String ControlFlowView_NextEventActionTooltip;
    public static String ControlFlowView_NextEventJobName;

    public static String ControlFlowView_PreviousEventActionName;
    public static String ControlFlowView_PreviousEventActionTooltip;
    public static String ControlFlowView_PreviousEventJobName;

    public static String ControlFlowView_birthTimeColumn;
    public static String ControlFlowView_threadPresentation;

    public static String ControlFlowView_tidColumn;
    public static String ControlFlowView_ptidColumn;
    public static String ControlFlowView_processColumn;
    public static String ControlFlowView_traceColumn;
    public static String ControlFlowView_invisibleColumn;

    public static String ControlFlowView_stateTypeName;
    public static String ControlFlowView_multipleStates;
    public static String ControlFlowView_nextProcessActionNameText;
    public static String ControlFlowView_nextProcessActionToolTipText;
    public static String ControlFlowView_previousProcessActionNameText;
    public static String ControlFlowView_previousProcessActionToolTipText;
    public static String ControlFlowView_followCPUBwdText;
    public static String ControlFlowView_followCPUFwdText;
    public static String ControlFlowView_checkActiveLabel;
    public static String ControlFlowView_checkActiveToolTip;
    public static String ControlFlowView_uncheckInactiveLabel;
    public static String ControlFlowView_uncheckInactiveToolTip;
    public static String ControlFlowView_attributeSyscallName;
    public static String ControlFlowView_attributeCpuName;
    public static String ControlFlowView_flatViewLabel;
    public static String ControlFlowView_flatViewToolTip;
    public static String ControlFlowView_hierarchicalViewLabel;
    public static String ControlFlowView_hierarchicalViewToolTip;
    public static String ControlFlowView_optimizeLabel;
    public static String ControlFlowView_optimizeToolTip;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
