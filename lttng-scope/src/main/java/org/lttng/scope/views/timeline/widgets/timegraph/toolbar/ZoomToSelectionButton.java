/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar;

import com.efficios.jabberwocky.common.TimeRange;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.lttng.scope.views.context.ViewGroupContextManager;
import org.lttng.scope.views.jfx.JfxImageFactory;
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget;

/**
 * Button to zoom into the current selection, if there is one.
 *
 * @author Alexandre Montplaisir
 */
class ZoomToSelectionButton extends Button {

    // TODO Get an icon!
    private static final String ZOOM_TO_SELECTION_ICON_PATH = "/icons/toolbar/zoom_in.gif"; //$NON-NLS-1$

    public ZoomToSelectionButton(TimeGraphWidget viewer) {
        Image icon = JfxImageFactory.instance().getImageFromResource(ZOOM_TO_SELECTION_ICON_PATH);
        setGraphic(new ImageView(icon));
        setTooltip(new Tooltip(Messages.sfZoomToSelectionActionDescription));
        setOnAction(e -> {
            TimeRange timeRange = ViewGroupContextManager.getCurrent().getSelectionTimeRange();
            /*
             * Only actually zoom if the selection is a time range, not a single timestamp.
             */
            if (timeRange.getDuration() > 0) {
                viewer.getControl().updateVisibleTimeRange(timeRange, true);
            }
        });
    }
}
