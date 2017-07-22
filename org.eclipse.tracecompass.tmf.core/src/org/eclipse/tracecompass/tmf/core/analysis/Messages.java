/*******************************************************************************
 * Copyright (c) 2013, 2015 École Polytechnique de Montréal
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for org.eclipse.tracecompass.tmf.core.analysis
 *
 * @author Geneviève Bastien
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String TmfAbstractAnalysisModule_TraceSetMoreThanOnce;
    public static String TmfAbstractAnalysisModule_AnalysisCannotExecute;
    public static String TmfAnalysisModuleHelper_AnalysisDoesNotApply;
    public static String TmfAbstractAnalysisModule_AnalysisForTrace;
    public static String TmfAbstractAnalysisModule_AnalysisModule;
    public static String TmfAbstractAnalysisModule_InvalidParameter;
    public static String TmfAbstractAnalysisModule_NullTrace;
    public static String TmfAbstractAnalysisModule_LabelId;
    public static String TmfAnalysis_RequirementInformation;
    public static String TmfAnalysis_RequirementMandatoryValues;
    public static String TmfAnalysis_RequirementNotFulfilled;
    public static String TmfAbstractAnalysisModule_RunningAnalysis;
    public static String TmfAnalysisManager_ErrorParameterProvider;
    public static String TmfAnalysisModuleHelper_ImpossibleToCreateModule;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
