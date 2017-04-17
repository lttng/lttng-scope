/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateInterval.LineThickness;

import com.google.common.base.MoreObjects;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * {@link Rectangle} object used to draw states in the timegraph. It attaches
 * the {@link TimeGraphStateInterval} that represents this state.
 *
 * @author Alexandre Montplaisir
 */
public class StateRectangle extends Rectangle {

    private final TimeGraphWidget fWidget;
    private final TimeGraphStateInterval fInterval;

    private final transient Paint fBaseColor;
    private final transient Paint fSelectedColor;

    private @Nullable Tooltip fTooltip = null;

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
    public StateRectangle(TimeGraphWidget viewer, TimeGraphStateInterval interval, int entryIndex) {
        fWidget = viewer;
        fInterval = interval;

        /*
         * It is possible, especially when re-opening already-indexed traces,
         * that the indexer and the state system do not report the same
         * start/end times. Make sure to clamp the interval's bounds to the
         * valid values.
         */
        TimeRange traceRange = viewer.getControl().getViewContext().getCurrentTraceFullRange();
        long traceStart = traceRange.getStart();
        long intervalStart = interval.getStartTime();
        double xStart = viewer.timestampToPaneXPos(Math.max(traceStart, intervalStart));

        long traceEnd = traceRange.getEnd();
        long intervalEndTime = interval.getEndTime();
        double xEnd = viewer.timestampToPaneXPos(Math.min(traceEnd, intervalEndTime));

        double width = Math.max(1.0, xEnd - xStart) + 1.0;
        double height = getHeightFromThickness(interval.getLineThickness());

        double yOffset = (TimeGraphWidget.ENTRY_HEIGHT - height) / 2;
        double y = entryIndex * TimeGraphWidget.ENTRY_HEIGHT + yOffset;

        setX(xStart);
        setY(y);
        setWidth(width);
        setHeight(height);

        double opacity = viewer.getDebugOptions().stateIntervalOpacity.get();
        setOpacity(opacity);

        /* Set a special paint for multi-state intervals */
        if (interval.isMultiState()) {
            Paint multiStatePaint = viewer.getDebugOptions().multiStatePaint.get();
            fBaseColor = multiStatePaint;
            fSelectedColor = multiStatePaint;
        } else {
            fBaseColor = JfxColorFactory.getColorFromDef(interval.getColorDefinition());
            fSelectedColor = JfxColorFactory.getDerivedColorFromDef(interval.getColorDefinition());
        }

        /* Set initial selection state and selection listener. */
        if (this.equals(viewer.getSelectedState())) {
            setSelected(true);
            viewer.setSelectedState(this, false);
        } else {
            setSelected(false);
        }

        setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) {
                return;
            }
            viewer.setSelectedState(this, true);
        });

        /*
         * Initialize the tooltip when the mouse enters the rectangle, if it was
         * not done previously.
         */
        setOnMouseEntered(e -> generateTooltip());
    }

    private void generateTooltip() {
        if (fTooltip != null) {
            return;
        }
        TooltipContents ttContents = new TooltipContents(fWidget.getDebugOptions());
        ttContents.appendRow(Messages.statePropertyElement, fInterval.getTreeElement().getName());
        ttContents.appendRow(Messages.statePropertyStateName, fInterval.getStateName());
        ttContents.appendRow(Messages.statePropertyStartTime, fInterval.getStartTime());
        ttContents.appendRow(Messages.statePropertyEndTime, fInterval.getEndTime());
        ttContents.appendRow(Messages.statePropertyDuration, fInterval.getDuration() + " ns"); //$NON-NLS-1$
        /* Add rows corresponding to the properties from the interval */
        Map<String, String> properties = fInterval.getProperties();
        properties.forEach((k, v) -> ttContents.appendRow(k, v));

        Tooltip tt = new Tooltip();
        tt.setGraphic(ttContents);
        Tooltip.install(this, tt);
        fTooltip = tt;
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
            hideTooltip();
        }
    }

    public void showTooltip(boolean beginning) {
        generateTooltip();
        Tooltip tt = requireNonNull(fTooltip);

        /*
         * Show the tooltip first, then move it to the correct location. It
         * needs to be shown for its getWidth() etc. to be populated.
         */
        tt.show(this, 0, 0);

        Point2D position;
        if (beginning) {
            /* Align to the bottom-left of the rectangle, left-aligned. */
            position = this.localToScreen(getX(), getY() + getHeight());
        } else {
            /* Align to the bottom-right of the rectangle, right-aligned */
            position = this.localToScreen(getX() + getWidth() - tt.getWidth(), getY() + getHeight());
        }

        tt.setAnchorX(position.getX());
        tt.setAnchorY(position.getY());
    }

    public void hideTooltip() {
        Tooltip tt = fTooltip;
        if (tt != null) {
            Platform.runLater(() -> {
                tt.hide();
            });
        }
    }

    @Override
    protected void finalize() {
        hideTooltip();
    }

    private static double getHeightFromThickness(LineThickness lt) {
        switch (lt) {
        case NORMAL:
        default:
            return TimeGraphWidget.ENTRY_HEIGHT - 4;
        case SMALL:
            return TimeGraphWidget.ENTRY_HEIGHT - 8;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(fWidget, fInterval);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StateRectangle other = (StateRectangle) obj;
        return Objects.equals(fWidget, other.fWidget)
                && Objects.equals(fInterval, other.fInterval);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("interval", fInterval) //$NON-NLS-1$
                .toString();
    }

    private static class TooltipContents extends GridPane {

        private final DebugOptions fOpts;

        private int nbRows = 0;

        public TooltipContents(DebugOptions opts) {
            fOpts = opts;
        }

        public void appendRow(Object... objects) {
            Node[] labels = Arrays.stream(objects)
                    .map(Object::toString)
                    .map(Text::new)
                    .peek(text -> {
                        text.fontProperty().bind(fOpts.toolTipFont);
                        text.fillProperty().bind(fOpts.toolTipFontFill);
                    })
                    .toArray(Node[]::new);
            addRow(nbRows++, labels);
        }
    }
}
