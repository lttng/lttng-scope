/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.ust.core.activator.internal;

import org.lttng.scope.common.core.ScopeCoreActivator;

/**
 * Plugin activator
 *
 * @noreference This class should not be accessed outside of this plugin.
 */
public class Activator extends ScopeCoreActivator {

    private static final String PLUGIN_ID = "org.lttng.scope.lttng.ust.core"; //$NON-NLS-1$

    /**
     * Return the singleton instance of this activator.
     *
     * @return The singleton instance
     */
    public static Activator instance() {
        return (Activator) ScopeCoreActivator.getInstance(PLUGIN_ID);
    }

    /**
     * Constructor
     */
    public Activator() {
        super(PLUGIN_ID);
    }

    @Override
    protected void startActions() {
    }

    @Override
    protected void stopActions() {
    }

}
