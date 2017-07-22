/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-Christian Kouamé - Initial API and implementation
 *   Patrick Tasse - Add support for source location
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import org.eclipse.osgi.util.NLS;

/**
 * Message strings for TMF model handling.
 *
 * @author Jean-Christian Kouamé
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String TmfAnalysisElement_HelperProperties;
    public static String TmfAnalysisElement_AnalysisProperties;
    public static String TmfAnalysisElement_InstantiateAnalysis;

    public static String TmfAnalysisViewOutput_ViewUnavailable;
    public static String TmfAnalysisViewOutput_Title;

    public static String TmfCommonProjectElement_ErrorClosingEditor;
    public static String TmfCommonProjectElement_ErrorRefreshingProperty;

    public static String TmfExperimentElement_ErrorInstantiatingTrace;
    public static String TmfExperimentElement_TypeName;

    public static String TmfTraceElement_ResourceProperties;
    public static String TmfTraceElement_TraceProperties;
    public static String TmfTraceElement_Name;
    public static String TmfTraceElement_Path;
    public static String TmfTraceElement_Location;
    public static String TmfTraceElement_EventType;
    public static String TmfTraceElement_TraceTypeId;
    public static String TmfTraceElement_IsLinked;
    public static String TmfTraceElement_SourceLocation;
    public static String TmfTraceElement_TimeOffset;
    public static String TmfTraceElement_LastModified;
    public static String TmfTraceElement_Size;
    public static String TmfTraceElement_FileSizeString;
    public static String TmfTraceElement_FolderSizeString;
    public static String TmfTraceElement_FolderSizeOverflowString;
    public static String TmfTraceElement_TypeName;

    public static String TmfViewsElement_Name;

    public static String TmfOnDemandAnalysesElement_Name;

    public static String TmfReportsElement_Name;

    public static String TmfTraceType_SelectTraceType;

    public static String TmfOpenTraceHelper_ErrorOpeningElement;
    public static String TmfOpenTraceHelper_LinkFailed;
    public static String TmfOpenTraceHelper_OpenElement;
    public static String TmfOpenTraceHelper_NoTraceOrExperimentType;
    public static String TmfOpenTraceHelper_NoTraceType;
    public static String TmfOpenTraceHelper_ErrorElement;
    public static String TmfOpenTraceHelper_InitError;
    public static String TmfOpenTraceHelper_TraceNotFound;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
