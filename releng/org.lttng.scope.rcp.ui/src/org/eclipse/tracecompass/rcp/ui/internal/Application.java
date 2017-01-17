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

package org.eclipse.tracecompass.rcp.ui.internal;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tracing.rcp.ui.messages.internal.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 * @author Bernd Hufmann
 */
public class Application implements IApplication {

    /**
     * The default workspace name
     */
    private static final String WORKSPACE_NAME = ".tracecompass"; //$NON-NLS-1$

    private Location fInstanceLoc = null;

    /**
     * Gets the tracing workspace root directory. By default it uses the user's
     * home directory. This value can be overwritten by using the global
     * TRACING_RCP_ROOT environment variable.
     *
     * @return the tracing workspace root directory
     */
    private static String getWorkspaceRoot() {
        /*
         * Look for the environment variable in the global environment variables
         */
        String workspaceRoot = System.getenv().get("TRACING_RCP_ROOT"); //$NON-NLS-1$
        if (workspaceRoot == null) {
            /* Use the user's home directory */
            workspaceRoot = System.getProperty("user.home"); //$NON-NLS-1$
        }
        return workspaceRoot;
    }

    @Override
    public Object start(IApplicationContext context) throws Exception {
        Display display = PlatformUI.createDisplay();
        try {
            // fetch the Location that we will be modifying
            fInstanceLoc = Platform.getInstanceLocation();

            // -data @noDefault in <applName>.ini allows us to set the workspace here.
            // If the user wants to change the location then he has to change
            // @noDefault to a specific location or remove -data @noDefault for
            // default location
            if (!fInstanceLoc.allowsDefault() && !fInstanceLoc.isSet()) {
                File workspaceRoot = new File(getWorkspaceRoot());

                if (!workspaceRoot.exists()) {
                    MessageDialog.openError(display.getActiveShell(),
                            Messages.Application_WorkspaceCreationError,
                            MessageFormat.format(Messages.Application_WorkspaceRootNotExistError, new Object[] { getWorkspaceRoot() }));
                    return IApplication.EXIT_OK;
                }

                if (!workspaceRoot.canWrite()) {
                    MessageDialog.openError(display.getActiveShell(),
                            Messages.Application_WorkspaceCreationError,
                            MessageFormat.format(Messages.Application_WorkspaceRootPermissionError, new Object[] { getWorkspaceRoot() }));
                    return IApplication.EXIT_OK;
                }

                String workspace = getWorkspaceRoot() + File.separator + WORKSPACE_NAME;
                // set location to workspace
                fInstanceLoc.set(new URL("file", null, workspace), false); //$NON-NLS-1$
            }

            if (!fInstanceLoc.lock()) {
                MessageDialog.openError(display.getActiveShell(),
                        Messages.Application_WorkspaceCreationError,
                        MessageFormat.format(Messages.Application_WorkspaceInUseError, new Object[] { fInstanceLoc.getURL().getPath() }));
                return IApplication.EXIT_OK;
            }

            int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
            if (returnCode == PlatformUI.RETURN_RESTART) {
                return IApplication.EXIT_RESTART;
            }
            return IApplication.EXIT_OK;
        } finally {
            display.dispose();
        }
    }

    @Override
    public void stop() {
        if (!PlatformUI.isWorkbenchRunning()) {
            return;
        }
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final Display display = workbench.getDisplay();
        fInstanceLoc.release();
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                if (!display.isDisposed()) {
                    workbench.close();
                }
            }
        });
    }
}
