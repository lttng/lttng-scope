/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.jabberwocky.common.ui;

import static org.lttng.jabberwocky.common.core.NonNullUtils.nullToEmptyString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author Alexandre Montplaisir
 */
public abstract class JabberwockyUIActivator extends AbstractUIPlugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** Map of all the registered activators, indexed by plugin ID */
    private static final Map<String, JabberwockyUIActivator> UI_ACTIVATORS =
            Collections.synchronizedMap(new HashMap<String, JabberwockyUIActivator>());

    /** This instance's plug-in ID */
    private final String fPluginId;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The constructor
     *
     * @param pluginID
     *            The ID of the plugin
     */
    public JabberwockyUIActivator(String pluginID) {
        fPluginId = pluginID;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Return this plug-in's ID.
     *
     * @return The plug-in ID
     */
    public String getPluginId() {
        return fPluginId;
    }

    /**
     * Get a registered activator. Subclasses should implement their own public
     * static getInstance() method, which returns the result of this.
     *
     * @param id
     *            The activator's plugin ID
     * @return The corresponding activator
     */
    protected static JabberwockyUIActivator getInstance(String id) {
        JabberwockyUIActivator ret = UI_ACTIVATORS.get(id);
        if (ret == null) {
            /* The activator should be registered at this point! */
            throw new IllegalStateException();
        }
        return ret;
    }

    /**
     * Get an {@link Image} from a path within the plugin.
     *
     * @param path
     *            The path to the image
     * @return The image object, or null if it could not be found
     */
    public @Nullable Image getImageFromPath(String path) {
        ImageDescriptor id = getImageDescripterFromPath(path);
        if (id == null) {
            return null;
        }
        return id.createImage();
    }

    /**
     * Get the image descriptor from a path within the plugin.
     *
     * @param path
     *            The path to the image
     *
     * @return The corresponding image descriptor, or null if the image is not
     *         found
     */
    public @Nullable ImageDescriptor getImageDescripterFromPath(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(fPluginId, path);
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    /**
     * Additional actions to run at the plug-in startup
     */
    protected abstract void startActions();

    /**
     * Additional actions to run at the plug-in shtudown
     */
    protected abstract void stopActions();

    // ------------------------------------------------------------------------
    // ore.eclipse.core.runtime.Plugin
    // ------------------------------------------------------------------------

    @Override
    public final void start(@Nullable BundleContext context) throws Exception {
        super.start(context);
        String id = this.getPluginId();
        synchronized (UI_ACTIVATORS) {
            if (UI_ACTIVATORS.containsKey(id)) {
                logError("Duplicate Activator ID : " + id); //$NON-NLS-1$
            }
            UI_ACTIVATORS.put(id, this);
        }
        startActions();
    }

    @Override
    public final void stop(@Nullable BundleContext context) throws Exception {
        stopActions();
        UI_ACTIVATORS.remove(this.getPluginId());
        super.stop(context);
    }

    // ------------------------------------------------------------------------
    // Logging helpers
    // ------------------------------------------------------------------------

    /**
     * Log a message with severity INFO.
     *
     * @param message
     *            The message to log
     * @param exception
     *            Optional exception to attach to the message
     */
    public void logInfo(@Nullable String message, Throwable... exception) {
        if (exception.length < 1) {
            getLog().log(new Status(IStatus.INFO, fPluginId, nullToEmptyString(message)));
        } else {
            getLog().log(new Status(IStatus.INFO, fPluginId, nullToEmptyString(message), exception[0]));
        }
    }


    /**
     * Log a message with severity WARNING.
     *
     * @param message
     *            The message to log
     * @param exception
     *            Optional exception to attach to the message
     */
    public void logWarning(@Nullable String message, Throwable... exception) {
        if (exception.length < 1) {
            getLog().log(new Status(IStatus.WARNING, fPluginId, nullToEmptyString(message)));
        } else {
            getLog().log(new Status(IStatus.WARNING, fPluginId, nullToEmptyString(message), exception[0]));
        }
    }

    /**
     * Log a message with severity ERROR.
     *
     * @param message
     *            The message to log
     * @param exception
     *            Optional exception to attach to the message
     */
    public void logError(@Nullable String message, Throwable... exception) {
        if (exception.length < 1) {
            getLog().log(new Status(IStatus.ERROR, fPluginId, nullToEmptyString(message)));
        } else {
            getLog().log(new Status(IStatus.ERROR, fPluginId, nullToEmptyString(message), exception[0]));
        }
    }

}
