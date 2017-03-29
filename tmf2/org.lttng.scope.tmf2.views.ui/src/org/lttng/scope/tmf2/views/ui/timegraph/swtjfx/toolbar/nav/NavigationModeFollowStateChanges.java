/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.toolbar.nav;

import static java.util.Objects.requireNonNull;

import org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.SwtJfxTimeGraphViewer;

/**
 * Navigation mode using state changes. It goes to the end/start of the current
 * selected state interval, or jumps to the next/previous one if we are already
 * at a border.
 *
 * @author Alexandre Montplaisir
 */
public class NavigationModeFollowStateChanges extends NavigationMode {

    private static final String BACK_ICON_PATH = "/icons/toolbar/nav_statechange_back.gif"; //$NON-NLS-1$
    private static final String FWD_ICON_PATH = "/icons/toolbar/nav_statechange_fwd.gif"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public NavigationModeFollowStateChanges() {
        super(requireNonNull(Messages.sfFollowStateChangesNavModeName),
                BACK_ICON_PATH,
                FWD_ICON_PATH);
    }

    @Override
    public void navigateBackwards(SwtJfxTimeGraphViewer viewer) {
        // TODO NYI
        System.out.println("Follow state changes backwards");
    }

    @Override
    public void navigateForwards(SwtJfxTimeGraphViewer viewer) {
        // TODO NYI
        System.out.println("Follow state changes forwards");
    }

}
