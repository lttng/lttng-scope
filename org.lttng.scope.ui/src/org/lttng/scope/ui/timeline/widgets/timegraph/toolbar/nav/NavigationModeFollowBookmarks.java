/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.timeline.widgets.timegraph.toolbar.nav;

import static java.util.Objects.requireNonNull;

import org.lttng.scope.ui.timeline.widgets.timegraph.TimeGraphWidget;

/**
 * Navigation mode using the current entry's events. It looks through all events
 * in the trace belonging to the tree entry of the current selected state, and
 * navigates through them. This allows stopping at events that may not cause a
 * state change shown in the view.
 *
 * @author Alexandre Montplaisir
 */
public class NavigationModeFollowBookmarks extends NavigationMode {

    private static final String BACK_ICON_PATH = "/icons/toolbar/nav_bookmark_back.gif"; //$NON-NLS-1$
    private static final String FWD_ICON_PATH = "/icons/toolbar/nav_bookmark_fwd.gif"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public NavigationModeFollowBookmarks() {
        super(requireNonNull(Messages.sfFollowBookmarksNavModeName),
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
        System.out.println("Follow bookmarks backwards");
    }

    @Override
    public void navigateForwards(TimeGraphWidget viewer) {
        // TODO NYI
        System.out.println("Follow bookmarks forwards");
    }

}
