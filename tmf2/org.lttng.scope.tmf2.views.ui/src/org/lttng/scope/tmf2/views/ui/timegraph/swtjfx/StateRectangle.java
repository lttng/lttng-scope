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
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval.LineThickness;

import com.google.common.base.MoreObjects;

import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

/**
 * {@link Rectangle} object used to draw states in the timegraph. It attaches
 * the {@link TimeGraphStateInterval} that represents this state.
 *
 * @author Alexandre Montplaisir
 */
public class StateRectangle extends Rectangle {

    private final TimeGraphStateInterval fInterval;

    private final transient Paint fBaseColor;
    private final transient Paint fSelectedColor;

    /**
     * Constructor
     *
     * @param viewer
     *            The viewer in which the rectangle will be placed
     * @param interval
     *            The source interval model object
     * @param entryIndex
     *            The index of the entry to which this state belongs.
     */
    public StateRectangle(SwtJfxTimeGraphViewer viewer, TimeGraphStateInterval interval, int entryIndex) {
        fInterval = interval;

        double xStart = viewer.timestampToPaneXPos(interval.getStartEvent().getTimestamp());

        /*
         * It is possible, especially when re-opening already-indexed traces,
         * that the indexer and the state system do not report the same end
         * time. Make sure to clamp the interval's end to the earliest valid
         * value.
         */
        long modelEndTime = viewer.getControl().getFullTimeGraphRange().getEnd();
        long intervalEndTime = interval.getEndEvent().getTimestamp();
        double xEnd = viewer.timestampToPaneXPos(Math.min(modelEndTime, intervalEndTime));

        double width = Math.max(1.0, xEnd - xStart) + 1.0;
        double height = getHeightFromThickness(interval.getLineThickness());

        double yOffset = (SwtJfxTimeGraphViewer.ENTRY_HEIGHT - height) / 2;
        double y = entryIndex * SwtJfxTimeGraphViewer.ENTRY_HEIGHT + yOffset;

        setX(xStart);
        setY(y);
        setWidth(width);
        setHeight(height);

        double opacity = viewer.getDebugOptions().getStateIntervalOpacity();
        setOpacity(opacity);

        /* Set a special paint for multi-state intervals */
        if (interval instanceof MultiStateInterval) {
            Stop[] stops = new Stop[] { new Stop(0, Color.BLACK), new Stop(1, Color.WHITE) };
            LinearGradient lg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
            fBaseColor = lg;
            fSelectedColor = lg;
        } else {
            fBaseColor = JfxColorFactory.getColorFromDef(interval.getColorDefinition());
            fSelectedColor = JfxColorFactory.getDerivedColorFromDef(interval.getColorDefinition());
        }

        setSelected(false);

        setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) {
                return;
            }
            viewer.setSelectedState(this);
        });
    }

    /**
     * Return the model interval representing this state
     *
     * @return The interval model object
     */
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

    private static double getHeightFromThickness(LineThickness lt) {
        switch (lt) {
        case NORMAL:
        default:
            return SwtJfxTimeGraphViewer.ENTRY_HEIGHT - 4;
        case SMALL:
            return SwtJfxTimeGraphViewer.ENTRY_HEIGHT - 8;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("interval", fInterval) //$NON-NLS-1$
                .toString();
    }

}
