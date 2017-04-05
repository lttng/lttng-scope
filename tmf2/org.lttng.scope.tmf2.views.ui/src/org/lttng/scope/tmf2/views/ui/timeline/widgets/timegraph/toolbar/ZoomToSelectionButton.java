/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar;

import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.ui.jfx.JfxImageFactory;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
            TmfTimeRange range = TmfTraceManager.getInstance().getCurrentTraceContext().getSelectionRange();
            TimeRange timeRange = TimeRange.fromTmfTimeRange(range);
            /*
             * Only actually zoom if the selection is a time range, not a single
             * timestamp.
             */
            if (timeRange.getDuration() > 0) {
                viewer.getControl().updateVisibleTimeRange(timeRange, true);
            }
        });
    }
}
