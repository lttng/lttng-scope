/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tracing.rcp.ui.messages.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for the tracing RCP.
 *
 * @author Bernd Hufmann
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String Application_WorkspaceCreationError;
    public static String Application_WorkspaceRootNotExistError;
    public static String Application_WorkspaceRootPermissionError;
    public static String Application_WorkspaceInUseError;
    public static String Application_WorkspaceSavingError;
    public static String Application_InternalError;

    public static String SplahScreen_VersionString;

    public static String CliParser_MalformedCommand;
    public static String CliParser_UnknownCommand;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
