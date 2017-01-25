/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common.core;

import static java.util.Objects.requireNonNull;
import static org.lttng.scope.common.core.NonNullUtils.nullToEmptyString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author Alexandre Montplaisir
 */
public abstract class ScopeCoreActivator extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** Map of all the registered activators, indexed by activator classes */
    private static final Map<Class<? extends ScopeCoreActivator>, ScopeCoreActivator> CORE_ACTIVATORS =
            Collections.synchronizedMap(new HashMap<Class<? extends ScopeCoreActivator>, ScopeCoreActivator>());

    /** This instance's plug-in ID */
    private final String fPluginId;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public ScopeCoreActivator() {
        fPluginId = requireNonNull(getBundle().getSymbolicName());
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
     * getInstance() method, which returns the result of this.
     *
     * @param activatorClass
     *            The activator's runtime class
     * @return The corresponding activator
     */
    protected static <T extends ScopeCoreActivator> T getInstance(Class<T> activatorClass) {
        ScopeCoreActivator activator = CORE_ACTIVATORS.get(activatorClass);
        if (activator == null) {
            /* The activator should be registered at this point! */
            throw new IllegalStateException();
        }
        /*
         * We inserted the corresponding class into the map ourselves, cast
         * should always be safe.
         */
        @SuppressWarnings("unchecked")
        T ret = (T) activator;
        return ret;
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
        Class<? extends ScopeCoreActivator> activatorClass = this.getClass();
        synchronized (CORE_ACTIVATORS) {
            if (CORE_ACTIVATORS.containsKey(activatorClass)) {
                logError("Duplicate Activator : " + activatorClass.getCanonicalName()); //$NON-NLS-1$
            }
            CORE_ACTIVATORS.put(activatorClass, this);
        }
        startActions();
    }

    @Override
    public final void stop(@Nullable BundleContext context) throws Exception {
        stopActions();
        CORE_ACTIVATORS.remove(this.getClass());
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
