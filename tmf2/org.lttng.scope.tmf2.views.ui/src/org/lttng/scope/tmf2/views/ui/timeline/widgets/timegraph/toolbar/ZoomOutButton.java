/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar;

import org.lttng.scope.tmf2.views.ui.jfx.JfxImageFactory;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;

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

    private static final String ZOOM_OUT_ICON_PATH = "/icons/toolbar/zoom_out.gif"; //$NON-NLS-1$

    public ZoomOutButton(TimeGraphWidget viewer) {
        Image icon = JfxImageFactory.instance().getImageFromResource(ZOOM_OUT_ICON_PATH);
        setGraphic(new ImageView(icon));
        setTooltip(new Tooltip(Messages.sfZoomOutActionDescription));
        setOnAction(e -> {
            // TODO Should pivot be the current selection, or just the center?
            viewer.getZoomActions().zoom(null, false);
        });
    }
}
