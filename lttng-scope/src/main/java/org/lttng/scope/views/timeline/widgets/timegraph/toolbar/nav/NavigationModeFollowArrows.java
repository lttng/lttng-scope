/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar.nav;

import static java.util.Objects.requireNonNull;

import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget;

/**
 * Navigation mode using drawn arrows.
 *
 * If we are currently positioned at an arrow endpoint, it follow the arrow to
 * its other endpoint, scrolling vertically if needed.
 *
 * If we are not at an arrow endpoint, following the current entry backwards or
 * forwards until we find one. If we don't find any, do nothing.
 *
 * @author Alexandre Montplaisir
 */
public class NavigationModeFollowArrows extends NavigationMode {

    private static final String BACK_ICON_PATH = "/icons/toolbar/nav_arrow_back.gif"; //$NON-NLS-1$
    private static final String FWD_ICON_PATH = "/icons/toolbar/nav_arrow_fwd.gif"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public NavigationModeFollowArrows() {
        super(requireNonNull(Messages.sfFollowArrowsNavModeName),
                BACK_ICON_PATH,
                FWD_ICON_PATH);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void navigateBackwards(TimeGraphWidget viewer) {
        // TODO NYI
        System.out.println("Follow arrows backwards");
    }

    @Override
    public void navigateForwards(TimeGraphWidget viewer) {
        // TODO NYI
        System.out.println("Follow arrows forwards");
    }

}
