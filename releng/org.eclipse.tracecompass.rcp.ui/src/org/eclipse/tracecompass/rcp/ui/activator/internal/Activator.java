/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.tracecompass.rcp.ui.activator.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.rcp.ui.cli.internal.CliParser;
import org.eclipse.tracecompass.rcp.ui.cli.internal.TracingRCPCliException;
import org.lttng.jabberwocky.common.ui.JabberwockyUIActivator;

/**
 * Plugin activator
 *
 * @noreference This class should not be accessed outside of this plugin.
 */
public class Activator extends JabberwockyUIActivator {

    private static final String PLUGIN_ID = "org.eclipse.tracecompass.rcp.ui"; //$NON-NLS-1$

    private static @Nullable CliParser fCli;

    /**
     * Return the singleton instance of this activator.
     *
     * @return The singleton instance
     */
    public static Activator instance() {
        return (Activator) JabberwockyUIActivator.getInstance(PLUGIN_ID);
    }

    /**
     * Constructor
     */
    public Activator() {
        super(PLUGIN_ID);
    }

    /**
     * Get the command-line parser.
     *
     * @return The CLI parser
     */
    public @Nullable CliParser getCli() {
        return fCli;
    }

    @Override
    protected void startActions() {
        String args[] = Platform.getCommandLineArgs();
        fCli = null;
        try {
            fCli = new CliParser(args);
        } catch (TracingRCPCliException e) {
            logError(e.getMessage());
        }
    }

    @Override
    protected void stopActions() {
        fCli = null;
    }

}
