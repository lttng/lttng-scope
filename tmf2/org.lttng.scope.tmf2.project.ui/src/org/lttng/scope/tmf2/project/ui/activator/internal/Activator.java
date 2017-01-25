/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.project.ui.activator.internal;

import org.lttng.scope.common.ui.ScopeUIActivator;

/**
 * The activator class controls the plug-in life cycle
 *
 * @noreference This class should not be accessed outside of this plugin.
 */
public class Activator extends ScopeUIActivator {

    /**
     * Return the singleton instance of this activator.
     *
     * @return The singleton instance
     */
    public static Activator instance() {
        return ScopeUIActivator.getInstance(Activator.class);
    }

    @Override
    protected void startActions() {
    }

    @Override
    protected void stopActions() {
    }

}
