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
import org.lttng.scope.views.jfx.JfxImageFactory;
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget;

/**
 * Button for zooming in. It should do the same action as one ctrl+mouse-scroll.
 *
 * @author Alexandre Montplaisir
 */
class ZoomInButton extends Button {

    private static final String ZOOM_IN_ICON_PATH = "/icons/toolbar/zoom_in.gif"; //$NON-NLS-1$

    public ZoomInButton(TimeGraphWidget viewer) {
        Image icon = JfxImageFactory.instance().getImageFromResource(ZOOM_IN_ICON_PATH);
        setGraphic(new ImageView(icon));
        setTooltip(new Tooltip(Messages.sfZoomInActionDescription));
        setOnAction(e -> {
            viewer.getZoomActions().zoom(true, false, null);
        });
    }
}
