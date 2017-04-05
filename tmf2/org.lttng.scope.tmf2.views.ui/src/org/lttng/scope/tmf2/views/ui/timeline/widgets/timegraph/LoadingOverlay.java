/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.Nullable;

import javafx.animation.FadeTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

class LoadingOverlay extends Rectangle {

    private static final Color LOADING_OVERLAY_COLOR = requireNonNull(Color.GRAY);
    private static final double LOADING_OVERLAY_OPACITY = 0.3;
    private static final double TRANSPARENT_OPACITY = 0.0;

    private static final double FULL_FADE_IN_DURATION_MILLIS = 100;
    private static final double FULL_FADE_OUT_DURATION_MILLIS = 100;

    private @Nullable FadeTransition fCurrentFadeIn;
    private @Nullable FadeTransition fCurrentFadeOut;

    public LoadingOverlay() {
        setFill(LOADING_OVERLAY_COLOR);
        setOpacity(TRANSPARENT_OPACITY);

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
        double startOpacity = getOpacity();
        /* Do a rule-of-three to determine the duration of fade-in we need. */
        double neededDuration = ((LOADING_OVERLAY_OPACITY - startOpacity) / LOADING_OVERLAY_OPACITY) * FULL_FADE_IN_DURATION_MILLIS;
        FadeTransition fadeIn = new FadeTransition(new Duration(neededDuration), this);
        fadeIn.setFromValue(startOpacity);
        fadeIn.setToValue(LOADING_OVERLAY_OPACITY);
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
        double startOpacity = getOpacity();
        /* Do a rule-of-three to determine the duration of fade-in we need. */
        double neededDuration = (startOpacity / LOADING_OVERLAY_OPACITY) * FULL_FADE_OUT_DURATION_MILLIS;
        FadeTransition fadeOut = new FadeTransition(new Duration(neededDuration), this);
        fadeOut.setFromValue(startOpacity);
        fadeOut.setToValue(TRANSPARENT_OPACITY);
        fadeOut.play();
        fCurrentFadeOut = fadeOut;
    }

}
