/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.toolbar;

import org.lttng.scope.tmf2.views.ui.jfx.JfxImageFactory;
import org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.SwtJfxTimeGraphViewer;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Button to zoom (out) to the full trace's range.
 *
 * @author Alexandre Montplaisir
 */
class ZoomToFullRangeButton extends Button {

    private static final String ZOOM_TO_FULL_RANGE_ICON_PATH = "/icons/toolbar/zoom_full.gif"; //$NON-NLS-1$

    public ZoomToFullRangeButton(SwtJfxTimeGraphViewer viewer) {
        Image icon = JfxImageFactory.instance().getImageFromResource(ZOOM_TO_FULL_RANGE_ICON_PATH);
        setGraphic(new ImageView(icon));
        setTooltip(new Tooltip(Messages.sfZoomToFullRangeActionDescription));
        setOnAction(e -> {
            /*
             * Grab the full trace range from the control, until it's moved to a
             * central property.
             */
            long start = viewer.getControl().getFullTimeGraphStartTime();
            long end = viewer.getControl().getFullTimeGraphEndTime();
            viewer.getControl().updateVisibleTimeRange(start, end, true);
        });
    }
}
