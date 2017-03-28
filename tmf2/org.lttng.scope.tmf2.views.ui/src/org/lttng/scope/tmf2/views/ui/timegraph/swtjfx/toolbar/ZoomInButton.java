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
 * Button for zooming in. It should do the same action as one ctrl+mouse-scroll.
 *
 * @author Alexandre Montplaisir
 */
class ZoomInButton extends Button {

    private final Image fZoomInIcon = new Image(getClass().getResourceAsStream("/icons/toolbar/zoom_in.gif")); //$NON-NLS-1$

    public ZoomInButton(SwtJfxTimeGraphViewer viewer) {
        setGraphic(new ImageView(fZoomInIcon));
        setTooltip(new Tooltip(Messages.sfZoomInActionDescription));
        setOnAction(e -> {
            // TODO Pivot could be the current time selection if it's in the
            // visible time range.
            viewer.getZoomActions().zoom(null, true);
        });
    }
}
