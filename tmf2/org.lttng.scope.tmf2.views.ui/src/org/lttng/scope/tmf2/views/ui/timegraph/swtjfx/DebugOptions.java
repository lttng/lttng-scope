/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

/**
 * Debug options for the {@link SwtJfxTimeGraphViewer}. Advanced users or unit
 * tests might want to modify these.
 *
 * @author Alexandre Montplaisir
 */
public class DebugOptions {

    /** Number of tree elements to print above *and* below the visible range */
    public int fEntryPrefetching = 5;

    /**
     * How much "padding" around the current visible window, on the left and
     * right, should be pre-rendered. Expressed as a fraction of the current
     * window (for example, 1.0 would render one "page" on each side).
     */
    public double fRenderRangePadding = 0.1;

    /** Time between UI updates, in milliseconds */
    public int fUIUpdateDelay = 250;

    /**
     * Whether the view should respond to vertical or horizontal scrolling
     * actions.
     */
    public boolean fScrollingListenersEnabled = true;

    /**
     * Constructor using the default options
     */
    public DebugOptions() {}

}
