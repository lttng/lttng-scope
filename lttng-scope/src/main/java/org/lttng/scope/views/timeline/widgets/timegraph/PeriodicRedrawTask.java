/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.context.ViewGroupContext;
import org.lttng.scope.views.timeline.TimelineWidget;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

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
public class PeriodicRedrawTask implements TimelineWidget.TimelineWidgetUpdateTask {

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
        if (!fViewer.getDebugOptions().isPaintingEnabled.get()) {
            return;
        }

        TimeRange currentHorizontalPos = fViewer.getControl().getViewContext().getCurrentVisibleTimeRange();
        VerticalPosition currentVerticalPos = fViewer.getCurrentVerticalPosition();

        boolean movedHorizontally;
        boolean movedVertically;

        if (fForceRedraw) {
            fForceRedraw = false;
            /*
             * Assume the visible window moved, to make sure everything gets
             * repainted on all layers.
             */
            movedHorizontally = true;
            movedVertically = true;
            /* Then skip the next checks */
        } else {
            movedHorizontally = !currentHorizontalPos.equals(fPreviousHorizontalPos);
            movedVertically = !currentVerticalPos.equals(fPreviousVerticalPosition);
            /*
             * Skip painting if the previous position is the exact same as last
             * time. Also skip if were not yet initialized.
             */
            if (!movedHorizontally && !movedVertically) {
                return;
            }
            if (currentHorizontalPos.equals(ViewGroupContext.UNINITIALIZED_RANGE)
                    || currentVerticalPos.equals(VerticalPosition.UNINITIALIZED_VP)) {
                return;
            }
        }

        fPreviousHorizontalPos = currentHorizontalPos;
        fPreviousVerticalPosition = currentVerticalPos;

        fViewer.paintArea(currentHorizontalPos, currentVerticalPos,
                movedHorizontally, movedVertically,
                fTaskSeq.getAndIncrement());
    }

    public void forceRedraw() {
        fForceRedraw = true;
    }

}
