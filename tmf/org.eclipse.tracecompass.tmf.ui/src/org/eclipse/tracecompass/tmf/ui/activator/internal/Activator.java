/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.tracecompass.tmf.ui.activator.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.ui.TmfUiRefreshHandler;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.viewers.events.TmfEventAdapterFactory;
import org.eclipse.tracecompass.tmf.ui.views.internal.TmfAlignmentSynchronizer;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.lttng.scope.common.ui.ScopeUIActivator;

/**
 * Plugin activator
 *
 * @noreference This class should not be accessed outside of this plugin.
 */
public class Activator extends ScopeUIActivator {

    private static final String PLUGIN_ID = "org.eclipse.tracecompass.tmf.ui"; //$NON-NLS-1$

    private static final String CORE_PLUGIN_ID = "org.eclipse.tracecompass.tmf.core"; //$NON-NLS-1$

    private @Nullable TmfEventAdapterFactory fTmfEventAdapterFactory;
    private @Nullable IPreferenceStore fCorePreferenceStore;

    /**
     * Return the singleton instance of this activator.
     *
     * @return The singleton instance
     */
    public static Activator instance() {
        return (Activator) ScopeUIActivator.getInstance(PLUGIN_ID);
    }

    /**
     * Constructor
     */
    public Activator() {
        super(PLUGIN_ID);
    }

    @Override
    protected void startActions() {
        TmfUiRefreshHandler.getInstance();
        TmfTraceElement.init();
        TmfExperimentElement.init();

        fTmfEventAdapterFactory = new TmfEventAdapterFactory();
        Platform.getAdapterManager().registerAdapters(fTmfEventAdapterFactory, ITmfEvent.class);
    }

    @Override
    protected void stopActions() {
        TmfUiRefreshHandler.getInstance().dispose();
        TmfAlignmentSynchronizer.getInstance().dispose();
        TmfProjectRegistry.dispose();

        Platform.getAdapterManager().unregisterAdapters(fTmfEventAdapterFactory);
    }

    @Override
    protected void initializeImageRegistry(@Nullable ImageRegistry reg) {
        if (reg == null) {
            return;
        }
        reg.put(ITmfImageConstants.IMG_UI_ZOOM, getImageFromPath(ITmfImageConstants.IMG_UI_ZOOM));
        reg.put(ITmfImageConstants.IMG_UI_ZOOM_IN, getImageFromPath(ITmfImageConstants.IMG_UI_ZOOM_IN));
        reg.put(ITmfImageConstants.IMG_UI_ZOOM_OUT, getImageFromPath(ITmfImageConstants.IMG_UI_ZOOM_OUT));
        reg.put(ITmfImageConstants.IMG_UI_SEQ_DIAGRAM_OBJ, getImageFromPath(ITmfImageConstants.IMG_UI_SEQ_DIAGRAM_OBJ));
        reg.put(ITmfImageConstants.IMG_UI_ARROW_COLLAPSE_OBJ, getImageFromPath(ITmfImageConstants.IMG_UI_ARROW_COLLAPSE_OBJ));
        reg.put(ITmfImageConstants.IMG_UI_ARROW_UP_OBJ, getImageFromPath(ITmfImageConstants.IMG_UI_ARROW_UP_OBJ));
        reg.put(ITmfImageConstants.IMG_UI_CONFLICT, getImageFromPath(ITmfImageConstants.IMG_UI_CONFLICT));
    }

     /**
      * Returns a preference store for org.eclipse.linux.tmf.core preferences
      * @return the preference store
      */
     public synchronized IPreferenceStore getCorePreferenceStore() {
         IPreferenceStore store = fCorePreferenceStore;
         if (store == null) {
             store = new ScopedPreferenceStore(InstanceScope.INSTANCE, CORE_PLUGIN_ID);
             fCorePreferenceStore = store;
         }
         return store;
     }
}
