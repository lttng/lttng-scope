/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.context;

import com.efficios.jabberwocky.context.ViewGroupContext;

/**
 * A common context for a group of views. Information is stored as properties,
 * and views can add listeners to get notified of value changes.
 *
 * @author Alexandre Montplaisir
 */
public class ViewGroupContextManager {

    /**
     * The context is a singleton for now, but the framework could be extended
     * to support several contexts (one per "view group") at the same time.
     */
    private static final ViewGroupContext INSTANCE = new ViewGroupContext();
    private static final SignalBridge SIGNAL_BRIDGE = new SignalBridge(INSTANCE);

    /**
     * For now, there is only a single view context for the framework. This
     * method returns this singleton instance.
     *
     * @return The view context
     */
    public static ViewGroupContext getCurrent() {
        return INSTANCE;
    }

    /**
     * Cleanup all view group contexts.
     */
    public static void cleanup() {
        SIGNAL_BRIDGE.dispose();
    }
}
