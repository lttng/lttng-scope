/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.operations.internal;

import org.eclipse.osgi.util.NLS;

/**
 * The messages for workspace operations.
 *
 * @author Bernd Hufmann
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String SelectTracesWizardPage_TraceRemovalTask;
    public static String SelectTracesWizardPage_TraceSelectionTask;
    public static String SelectTracesWizardPage_SelectionError;
    public static String NewExperimentOperation_CreationError;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
