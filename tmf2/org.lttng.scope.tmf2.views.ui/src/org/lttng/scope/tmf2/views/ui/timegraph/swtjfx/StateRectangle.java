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

import com.google.common.base.MoreObjects;

import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class StateRectangle extends Rectangle {

    private final TimeGraphStateInterval fInterval;

    private final transient Color fBaseColor;
    private final transient Color fSelectedColor;

    public StateRectangle(SwtJfxTimeGraphViewer viewer, TimeGraphStateInterval interval, int entryIndex) {
        fInterval = interval;

        double xStart = viewer.timestampToPaneXPos(interval.getStartEvent().getTimestamp());

        /*
         * It is possible, especially when re-opening already-indexed traces,
         * that the indexer and the state system do not report the same end
         * time. Make sure to clamp the interval's end to the earliest valid
         * value.
         */
        long modelEndTime = viewer.getControl().getFullTimeGraphEndTime();
        long intervalEndTime = interval.getEndEvent().getTimestamp();
        double xEnd = viewer.timestampToPaneXPos(Math.min(modelEndTime, intervalEndTime));

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

        double yOffset = (SwtJfxTimeGraphViewer.ENTRY_HEIGHT - height) / 2;
        double y = entryIndex * SwtJfxTimeGraphViewer.ENTRY_HEIGHT + yOffset;

        setX(xStart);
        setY(y);
        setWidth(width);
        setHeight(height);

        double opacity = viewer.getDebugOptions().getStateIntervalOpacity();
        setOpacity(opacity);

        fBaseColor = JfxColorFactory.getColorFromDef(interval.getColorDefinition());
        fSelectedColor = JfxColorFactory.getDerivedColorFromDef(interval.getColorDefinition());

        setSelected(false);

        setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) {
                return;
            }
            viewer.intervalSelected(this);
        });
    }

    public TimeGraphStateInterval getStateInterval() {
        return fInterval;
    }

    public void setSelected(boolean isSelected) {
        if (isSelected) {
            setFill(fSelectedColor);
        } else {
            setFill(fBaseColor);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("interval", fInterval) //$NON-NLS-1$
                .toString();
    }

}
