/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.lttng.kernel.core.activator.internal;

import org.lttng.scope.common.core.ScopeCoreActivator;
import org.lttng.scope.lttng.kernel.core.views.timegraph.resources.ResourcesCpuIrqModelProvider;
import org.lttng.scope.lttng.kernel.core.views.timegraph.resources.ResourcesIrqModelProvider;
import org.lttng.scope.lttng.kernel.core.views.timegraph.threads.ThreadsModelProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.TimeGraphModelProviderManager;

/**
 * Plugin activator
 *
 * @noreference This class should not be accessed outside of this plugin.
 */
public class Activator extends ScopeCoreActivator {

    /**
     * Return the singleton instance of this activator.
     *
     * @return The singleton instance
     */
    public static Activator instance() {
        return ScopeCoreActivator.getInstance(Activator.class);
    }

    @Override
    protected void startActions() {
        /* Register the model providers shipped in this plugin */
        TimeGraphModelProviderManager manager = TimeGraphModelProviderManager.instance();
        manager.registerProviderFactory(() -> new ThreadsModelProvider());
        manager.registerProviderFactory(() -> new ResourcesCpuIrqModelProvider());
        manager.registerProviderFactory(() -> new ResourcesIrqModelProvider());

        /* Register the built-in LTTng-Analyses descriptors */
//        try {
//            LttngAnalysesLoader.load();
//        } catch (LamiAnalysisFactoryException | IOException e) {
//            // Not the end of the world if the analyses are not available
//            logWarning("Cannot find LTTng analyses configuration files: " + e.getMessage()); //$NON-NLS-1$
//        }
    }

    @Override
    protected void stopActions() {
    }

}
