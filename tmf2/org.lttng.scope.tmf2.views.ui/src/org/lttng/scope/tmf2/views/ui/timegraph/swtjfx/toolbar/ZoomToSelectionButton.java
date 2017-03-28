/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.toolbar;

import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.SwtJfxTimeGraphViewer;

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

    private final Image fZoomSelectionIcon = new Image(getClass().getResourceAsStream("/icons/toolbar/zoom_in.gif")); //$NON-NLS-1$

    public ZoomToSelectionButton(SwtJfxTimeGraphViewer viewer) {
        setGraphic(new ImageView(fZoomSelectionIcon));
        setTooltip(new Tooltip(Messages.sfZoomToSelectionActionDescription));
        setOnAction(e -> {
            TmfTimeRange range = TmfTraceManager.getInstance().getCurrentTraceContext().getSelectionRange();
            long start = range.getStartTime().toNanos();
            long end = range.getEndTime().toNanos();
            /*
             * Only actually zoom if the selection is a time range, not a single
             * timestamp.
             */
            if (start != end) {
                viewer.getControl().updateVisibleTimeRange(start, end, true);
            }
        });
    }
}
