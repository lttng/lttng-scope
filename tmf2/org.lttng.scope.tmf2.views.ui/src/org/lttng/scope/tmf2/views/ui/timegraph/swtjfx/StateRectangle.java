/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;

import javafx.scene.shape.Rectangle;

public class StateRectangle extends Rectangle {

    private final TimeGraphStateInterval fInterval;

    public StateRectangle(SwtJfxTimeGraphViewer viewer, TimeGraphStateInterval interval, int entryIndex) {
        fInterval = interval;

        double xStart = viewer.timestampToPaneXPos(interval.getStartEvent().getTimestamp());
        double xEnd = viewer.timestampToPaneXPos(interval.getEndEvent().getTimestamp());
        double width = Math.max(1.0, xEnd - xStart) + 1.0;

        double height;
        switch (interval.getLineThickness()) {
        case NORMAL:
        default:
            height = SwtJfxTimeGraphViewer.ENTRY_HEIGHT - 4;
            break;
        case SMALL:
            height = SwtJfxTimeGraphViewer.ENTRY_HEIGHT - 8;
            break;
        }

        // TODO Calculate value for small thickness too
        double y = entryIndex * SwtJfxTimeGraphViewer.ENTRY_HEIGHT + 2;

        setX(xStart);
        setY(y);
        setWidth(width);
        setHeight(height);
        setFill(JfxColorFactory.getColorFromDef(interval.getColorDefinition()));
    }

    public TimeGraphStateInterval getStateInterval() {
        return fInterval;
    }

}
