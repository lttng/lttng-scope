/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.timeline.widgets.timegraph;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.ui.timeline.DebugOptions;

import javafx.animation.FadeTransition;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

class LoadingOverlay extends Rectangle {

    private final DebugOptions fOpts;

    private @Nullable FadeTransition fCurrentFadeIn;
    private @Nullable FadeTransition fCurrentFadeOut;

    public LoadingOverlay(DebugOptions opts) {
        fOpts = opts;

        /*
         * Set the fill (color) by binding the property to the corresponding
         * config option. That way if the use changes the configured value, this
         * overlay will follow.
         */
        fillProperty().bind(fOpts.loadingOverlayColor);

        /*
         * The opacity property on the other hand will change through normal
         * operation of the overlay. We are just setting the initial value here,
         * no permanent bind.
         */
        setOpacity(fOpts.loadingOverlayTransparentOpacity.get());

        /*
         * The overlay should not catch mouse events. Note we could use
         * .setPickOnBounds(false) if we wanted to handle events but also allow
         * them to go "through".
         */
        setMouseTransparent(true);
    }

    public synchronized void fadeIn() {
        if (fCurrentFadeIn != null) {
            /* We're already fading in, let it continue. */
            return;
        }
        if (fCurrentFadeOut != null) {
            /*
             * Don't use stop() because that would revert to the initial opacity
             * right away.
             */
            fCurrentFadeOut.pause();
            fCurrentFadeOut = null;
        }

        double fullOpacity = fOpts.loadingOverlayFullOpacity.get();
        double fullFadeInDuration = fOpts.loadingOverlayFadeInDuration.get();
        double startOpacity = getOpacity();

        /* Do a rule-of-three to determine the duration of fade-in we need. */
        double neededDuration = ((fullOpacity - startOpacity) / fullOpacity) * fullFadeInDuration;
        FadeTransition fadeIn = new FadeTransition(new Duration(neededDuration), this);
        fadeIn.setFromValue(startOpacity);
        fadeIn.setToValue(fullOpacity);
        fadeIn.play();
        fCurrentFadeIn = fadeIn;
    }

    public synchronized void fadeOut() {
        if (fCurrentFadeOut != null) {
            /* We're already fading out, let it continue. */
            return;
        }
        if (fCurrentFadeIn != null) {
            fCurrentFadeIn.pause();
            fCurrentFadeIn = null;
        }

        double fullOpacity = fOpts.loadingOverlayFullOpacity.get();
        double transparentOpacity = fOpts.loadingOverlayTransparentOpacity.get();
        double fullFadeOutDuration = fOpts.loadingOverlayFadeOutDuration.get();
        double startOpacity = getOpacity();

        /* Do a rule-of-three to determine the duration of fade-in we need. */
        double neededDuration = (startOpacity / fullOpacity) * fullFadeOutDuration;
        FadeTransition fadeOut = new FadeTransition(new Duration(neededDuration), this);
        fadeOut.setFromValue(startOpacity);
        fadeOut.setToValue(transparentOpacity);
        fadeOut.play();
        fCurrentFadeOut = fadeOut;
    }

}
