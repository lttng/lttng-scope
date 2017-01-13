/**********************************************************************
 * Copyright (c) 2013, 2017 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.commands.internal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.ui.activator.internal.Activator;
import org.eclipse.tracecompass.tmf.ui.activator.internal.ITmfUIPreferences;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Open directory handler
 */
public class OpenDirectoryHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) {

        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        /*
         * menuSelection will be null if invoked from the application menu bar,
         * but not for contextual menus.
         */
        @Nullable ISelection menuSelection = HandlerUtil.getActiveMenuSelection(event);

        /* Determine where we will put the newly opened trace */
        TmfTraceFolder destinationFolder;
        if ((menuSelection != null) && (currentSelection instanceof IStructuredSelection)) {
            /*
             * If handler is called from the context sensitive menu of a tracing
             * project, import to the traces folder from this project.
             */
            destinationFolder = TmfHandlerUtil.getTraceFolderFromSelection(currentSelection);
        } else {
            /*
             * If handler is called from the file menu, import into the default
             * tracing project.
             */
            IProject project = TmfProjectRegistry.createProject(
                    TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME, null, new NullProgressMonitor());
            TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
            destinationFolder = projectElement.getTracesFolder();
        }

        /* Get trace to open */
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        DirectoryDialog dd = new DirectoryDialog(shell);
        dd.setText(Messages.OpenDirHandler_SelectTraceDirectory);

        /* Restore the previously-saved path if there is one */
        IEclipsePreferences defaultPreferences = InstanceScope.INSTANCE.getNode(Activator.instance().getPluginId());
        String lastLocation = defaultPreferences.get(ITmfUIPreferences.PREF_SAVED_OPEN_FILE_LOCATION, null);
        if (lastLocation != null && !lastLocation.isEmpty()) {
            Path parentPath = Paths.get(lastLocation).getParent();
            if (parentPath != null && Files.exists(parentPath)) {
                dd.setFilterPath(parentPath.toString());
            }
        }

        String filePath = dd.open();
        if (filePath == null) {
            /* Dialog was cancelled by the user */
            return null;
        }

        try {
            TmfOpenTraceHelper.openTraceFromPath(destinationFolder, filePath, shell);
        } catch (CoreException e) {
            Activator.instance().logError(e.getMessage(), e);
        }

        /* Save the user-selected path so that next open operations start from this location */
        InstanceScope.INSTANCE.getNode(Activator.instance().getPluginId()).put(ITmfUIPreferences.PREF_SAVED_OPEN_FILE_LOCATION, filePath);
        return null;
    }
}
