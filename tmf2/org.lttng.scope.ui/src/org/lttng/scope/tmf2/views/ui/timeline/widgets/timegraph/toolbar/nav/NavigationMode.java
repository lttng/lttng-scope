/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar.nav;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.ui.jfx.JfxImageFactory;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;

import javafx.scene.image.Image;

/**
 * Abstract class for defining navigation (back and forward buttons) modes in
 * the toolbar.
 *
 * @author Alexandre Montplaisir
 */
public abstract class NavigationMode {

    private final String fModeName;
    private final @Nullable Image fBackIcon;
    private final @Nullable Image fForwardIcon;

    /**
     * Constructor
     *
     * @param modeName
     *            Name of the mode, will be shown in the UI
     * @param backIconPath
     *            The icon to use for the "back" button
     * @param forwardIconPath
     *            The icon to use for the "forward" button
     */
    public NavigationMode(String modeName, String backIconPath, String forwardIconPath) {
        fModeName = modeName;

        JfxImageFactory factory = JfxImageFactory.instance();
        fBackIcon = factory.getImageFromResource(backIconPath);
        fForwardIcon = factory.getImageFromResource(forwardIconPath);
    }

    /**
     * Get the name of this navigation mode.
     *
     * @return The mode name
     */
    public String getModeName() {
        return fModeName;
    }

    /**
     * Get the icon to use for the "back" button.
     *
     * @return The back button icon
     */
    public @Nullable Image getBackIcon() {
        return fBackIcon;
    }

    /**
     * Get the icon to use for the "forward" button.
     *
     * @return The forward button icon
     */
    public @Nullable Image getForwardIcon() {
        return fForwardIcon;
    }

    /**
     * If for some reason this particular mode should not be available to the
     * user, this should be overridden to return 'false'.
     *
     * @return If this mode is enabled
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * What to do when the back button is invoked while in this mode.
     *
     * @param viewer
     *            The viewer on which we are working
     */
    public abstract void navigateBackwards(TimeGraphWidget viewer);

    /**
     * What to do when the forward button is invoked while in this mode.
     *
     * @param viewer
     *            The viewer on which we are working
     */
    public abstract void navigateForwards(TimeGraphWidget viewer);
}