/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.lttng.scope.common.jfx.JfxImageFactory;
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget;

/**
 * Button for zooming out. It should do the same action as one
 * ctrl+mouse-scroll.
 *
 * @author Alexandre Montplaisir
 */
class ZoomOutButton extends Button {

    private static final String ZOOM_OUT_ICON_PATH = "/icons/toolbar/zoom_out.gif"; //$NON-NLS-1$

    public ZoomOutButton(TimeGraphWidget viewer) {
        Image icon = JfxImageFactory.getImageFromResource(ZOOM_OUT_ICON_PATH);
        setGraphic(new ImageView(icon));
        setTooltip(new Tooltip(Messages.sfZoomOutActionDescription));
        setOnAction(e -> {
            viewer.getZoomActions().zoom(false, false, null);
        });
    }
}
