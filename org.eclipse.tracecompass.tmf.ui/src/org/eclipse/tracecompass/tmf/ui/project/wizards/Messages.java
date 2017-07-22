/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Add support for folder elements
 *   Marc-Andre Laperle - Preserve folder structure on import
 *   Bernd Hufmann - Extract ImportTraceWizard messages
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.wizards;

import org.eclipse.osgi.util.NLS;

/**
 * Message strings for TMF model handling.
 *
 * @author Francois Chouinard
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String NewProjectWizard_DialogHeader;
    public static String NewProjectWizard_DialogMessage;
    public static String SelectTracesWizard_WindowTitle;
    public static String SelectTracesWizardPage_TraceColumnHeader;
    public static String SelectTracesWizardPage_WindowTitle;
    public static String SelectTracesWizardPage_Description;
    public static String SelectTracesWizardPage_SelectionError;
    public static String SelectTracesWizardPage_SelectionOperationCancelled;
    public static String SelectTracesWizardPage_InternalErrorTitle;
    public static String Dialog_EmptyNameError;
    public static String Dialog_ExistingNameError;
    public static String NewExperimentDialog_DialogTitle;
    public static String NewExperimentDialog_ExperimentName;
    public static String RenameExperimentDialog_DialogTitle;
    public static String RenameExperimentDialog_ExperimentName;
    public static String RenameExperimentDialog_ExperimentNewName;
    public static String CopyExperimentDialog_DialogTitle;
    public static String CopyExperimentDialog_ExperimentName;
    public static String CopyExperimentDialog_ExperimentNewName;
    public static String RenameTraceDialog_DialogTitle;
    public static String RenameTraceDialog_TraceName;
    public static String RenameTraceDialog_TraceNewName;
    public static String CopyTraceDialog_DialogTitle;
    public static String CopyTraceDialog_TraceName;
    public static String CopyTraceDialog_TraceNewName;
    public static String NewFolderDialog_DialogTitle;
    public static String NewFolderDialog_FolderName;
    public static String RenameFolderDialog_DialogTitle;
    public static String RenameFolderDialog_FolderName;
    public static String RenameFolderDialog_FolderNewName;
    public static String SelectRootNodeWizard_WindowTitle;
    public static String SelectRootNodeWizardPage_WindowTitle;
    public static String SelectRootNodeWizardPage_Description;
    public static String SelectRootNodeWizardPage_TraceColumnHeader;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
