/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.views.resources.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Softirq names. Not the C style ones, but descriptive ones
 *
 * @author Matthew Khouzam
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String SoftIrqLabelProvider_softIrq0;
    public static String SoftIrqLabelProvider_softIrq1;
    public static String SoftIrqLabelProvider_softIrq2;
    public static String SoftIrqLabelProvider_softIrq3;
    public static String SoftIrqLabelProvider_softIrq4;
    public static String SoftIrqLabelProvider_softIrq5;
    public static String SoftIrqLabelProvider_softIrq6;
    public static String SoftIrqLabelProvider_softIrq7;
    public static String SoftIrqLabelProvider_softIrq8;
    public static String SoftIrqLabelProvider_softIrq9;
    public static String SoftIrqLabelProvider_Unknown;

    public static String ResourcesView_stateTypeName;
    public static String ResourcesView_multipleStates;
    public static String ResourcesView_nextResourceActionNameText;
    public static String ResourcesView_nextResourceActionToolTipText;
    public static String ResourcesView_previousResourceActionNameText;
    public static String ResourcesView_previousResourceActionToolTipText;
    public static String ResourcesView_attributeCpuName;
    public static String ResourcesView_attributeIrqName;
    public static String ResourcesView_attributeSoftIrqName;
    public static String ResourcesView_attributeHoverTime;
    public static String ResourcesView_attributeTidName;
    public static String ResourcesView_attributeProcessName;
    public static String ResourcesView_attributeSyscallName;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
