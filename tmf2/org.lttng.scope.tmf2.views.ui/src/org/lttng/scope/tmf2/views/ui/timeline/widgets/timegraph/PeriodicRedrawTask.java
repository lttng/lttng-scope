/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.context.ViewGroupContext;

/**
 * It was quickly determined that having mouse listeners start repaint tasks
 * directly is not a good solution, since simply enqueuing a job can potentially
 * take time, and thus makes scrolling sluggish and annoying to use.
 *
 * Instead, a separate thread can look periodically if the current view's
 * position has changed since its last check, and queue UI updates as needed.
 *
 * This class implements such thread, as a {@link TimerTask}.
 *
 * @author Alexandre Montplaisir
 */
class PeriodicRedrawTask extends TimerTask {

    /**
     * Sequence number attached to each redraw operation. Can be used for
     * tracing/analysis.
     */
    private final AtomicLong fTaskSeq = new AtomicLong();
    private final TimeGraphWidget fViewer;

    private TimeRange fPreviousHorizontalPos = ViewGroupContext.UNINITIALIZED_RANGE;
    private VerticalPosition fPreviousVerticalPosition = VerticalPosition.UNINITIALIZED_VP;

    private volatile boolean fForceRedraw = false;

    public PeriodicRedrawTask(TimeGraphWidget viewer) {
        fViewer = viewer;
    }

    @Override
    public void run() {
        if (!fViewer.getDebugOptions().isPaintingEnabled()) {
            return;
        }

        TimeRange currentHorizontalPos = fViewer.getControl().getViewContext().getCurrentVisibleTimeRange();
        VerticalPosition currentVerticalPos = fViewer.getCurrentVerticalPosition();

        if (fForceRedraw) {
            fForceRedraw = false;
            /* Then skip the next checks */
        } else {
            /*
             * Skip painting if the previous position is the exact same as last
             * time. Also skip if were not yet initialized.
             */
            if (currentHorizontalPos.equals(fPreviousHorizontalPos)
                    && currentVerticalPos.equals(fPreviousVerticalPosition)) {
                return;
            }
            if (currentHorizontalPos.equals(ViewGroupContext.UNINITIALIZED_RANGE)
                    || currentVerticalPos.equals(VerticalPosition.UNINITIALIZED_VP)) {
                return;
            }
        }

        fPreviousHorizontalPos = currentHorizontalPos;
        fPreviousVerticalPosition = currentVerticalPos;

        fViewer.paintBackground(currentVerticalPos);
        fViewer.paintArea(currentHorizontalPos, currentVerticalPos, fTaskSeq.getAndIncrement());
    }

    public void forceRedraw() {
        fForceRedraw = true;
    }

}
