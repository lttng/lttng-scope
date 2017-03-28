/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.toolbar;

import org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.SwtJfxTimeGraphViewer;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Button for zooming out. It should do the same action as one
 * ctrl+mouse-scroll.
 *
 * @author Alexandre Montplaisir
 */
class ZoomOutButton extends Button {

    private final Image fZoomOutIcon = new Image(getClass().getResourceAsStream("/icons/toolbar/zoom_out.gif")); //$NON-NLS-1$

    public ZoomOutButton(SwtJfxTimeGraphViewer viewer) {
        setGraphic(new ImageView(fZoomOutIcon));
        setTooltip(new Tooltip(Messages.sfZoomOutActionDescription));
        setOnAction(e -> {
            // TODO Should pivot be the current selection, or just the center?
            viewer.getZoomActions().zoom(null, false);
        });
    }
}
