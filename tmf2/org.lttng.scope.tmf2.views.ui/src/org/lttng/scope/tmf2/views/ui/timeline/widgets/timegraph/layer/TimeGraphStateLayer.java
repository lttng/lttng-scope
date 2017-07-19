/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.layer;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.ui.jfx.JfxUtils;
import org.lttng.scope.tmf2.views.ui.timeline.DebugOptions;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.StateRectangle;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.VerticalPosition;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.timegraph.model.provider.states.ITimeGraphModelStateProvider;
import com.efficios.jabberwocky.timegraph.model.render.states.TimeGraphStateRender;
import com.efficios.jabberwocky.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.timegraph.model.render.tree.TimeGraphTreeRender;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.OverrunStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Time graph layer taking care of drawing the state intervals and their labels.
 *
 * Intervals and labels are part of separate Groups, which the time graph widget
 * can stack in the correct order. This ensures that the labels are always shown
 * on top of the states.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphStateLayer extends TimeGraphLayer {

    private final Group fLabelGroup = new Group();
    private final ITimeGraphModelStateProvider fStateProvider;

    private TimeRange fWindowRange;

    /**
     * Constructor
     *
     * @param widget
     *            Time graph widget to which this layer belongs
     * @param parentGroup
     *            The group to which this layer should add its children
     */
    public TimeGraphStateLayer(TimeGraphWidget widget, Group parentGroup) {
        super(widget, parentGroup);

        fStateProvider = widget.getControl().getModelRenderProvider().getStateProvider();
        /*
         * Initially we'll set our window range to the one from the view
         * context, afterwards we'll let the widget update it.
         */
        fWindowRange = widget.getControl().getViewContext().getCurrentVisibleTimeRange();
    }

    /**
     * Return the separate Group used to hold the state labels.
     *
     * @return The group of state labels
     */
    public Group getLabelGroup() {
        return fLabelGroup;
    }

    /**
     * Update the window range tracked by this layer. This will be used to
     * "clamp" the state labels to the current visible window in case the state
     * intervals are larger than it.
     *
     * It may or may not be the same as the current UI visible range.
     *
     * @param windowRange
     *            The new window range
     */
    public void setWindowRange(TimeRange windowRange) {
        fWindowRange = windowRange;
    }

    @Override
    public void drawContents(TimeGraphTreeRender treeRender, TimeRange timeRange,
            VerticalPosition vPos, @Nullable FutureTask<?> task) {

        final long resolution = Math.max(1, Math.round(getWidget().getCurrentNanosPerPixel()));
        final List<TimeGraphTreeElement> allTreeElements = treeRender.getAllTreeElements();
        final int nbElements = allTreeElements.size();
        final int entriesToPrefetch = getWidget().getDebugOptions().entryPadding.get();
        final int topEntry = Math.max(0,
                TimeGraphWidget.paneYPosToEntryListIndex(vPos.fTopPos, TimeGraphWidget.ENTRY_HEIGHT) - entriesToPrefetch);
        final int bottomEntry = Math.min(nbElements,
                TimeGraphWidget.paneYPosToEntryListIndex(vPos.fBottomPos, TimeGraphWidget.ENTRY_HEIGHT) + entriesToPrefetch);

        System.out.println("topEntry=" + topEntry +", bottomEntry=" + bottomEntry);

        List<TimeGraphStateRender> stateRenders = allTreeElements.subList(topEntry, bottomEntry).stream()
                .map(treeElem -> fStateProvider.getStateRender(treeElem, timeRange, resolution, task))
                .collect(Collectors.toList());

        if (task != null && task.isCancelled()) {
            return;
        }

        Collection<StateRectangle> stateRectangles = prepareStateRectangles(stateRenders, topEntry);
        Node statesLayerContents = prepareTimeGraphStatesContents(stateRectangles);
        Node labelsLayerContents = prepareTimeGrahLabelsContents(stateRectangles, fWindowRange);

        /*
         * Go over all state rectangles, and bring the "multi-state"
         * ones to the front, to be sure they show on top of the others.
         * Note we cannot do the forEach() as part of the stream, that
         * would throw a ConcurrentModificationException.
         */
        ((Group) statesLayerContents).getChildren().stream()
                .map(node -> (StateRectangle) node)
                .filter(rect -> (rect.getStateInterval().isMultiState()))
                .collect(Collectors.toList())
                .forEach(Node::toFront);

        Platform.runLater(() -> {
            getParentGroup().getChildren().clear();
            getLabelGroup().getChildren().clear();

            getParentGroup().getChildren().add(statesLayerContents);
            getLabelGroup().getChildren().add(labelsLayerContents);
        });
    }

    @Override
    public void clear() {
        JfxUtils.runOnMainThread(() -> {
            getParentGroup().getChildren().clear();
            getLabelGroup().getChildren().clear();
        });
    }

    private Collection<StateRectangle> prepareStateRectangles(
            List<TimeGraphStateRender> stateRenders, int topEntry) {
        /* Prepare the colored state rectangles */
        Collection<StateRectangle> rectangles = IntStream.range(0, stateRenders.size()).parallel()
                .mapToObj(idx -> getRectanglesForStateRender(stateRenders.get(idx), idx + topEntry))
                .flatMap(Function.identity())
                .collect(Collectors.toSet());
        return rectangles;
    }

    private Stream<StateRectangle> getRectanglesForStateRender(TimeGraphStateRender stateRender, int entryIndex) {
        return stateRender.getStateIntervals().stream()
                .map(interval -> new StateRectangle(getWidget(), interval, entryIndex));
    }

    private static Node prepareTimeGraphStatesContents(Collection<StateRectangle> stateRectangles) {
        Group group = new Group();
        group.getChildren().addAll(stateRectangles);
        return group;
    }

    private Node prepareTimeGrahLabelsContents(Collection<StateRectangle> stateRectangles,
            TimeRange windowRange) {
        double minX = getWidget().timestampToPaneXPos(windowRange.getStartTime());

        final String ellipsisStr = DebugOptions.ELLIPSIS_STRING;
        final double ellipsisWidth = getWidget().getDebugOptions().getEllipsisWidth();
        final Font textFont = getWidget().getDebugOptions().stateLabelFont.get();
        final OverrunStyle overrunStyle = OverrunStyle.ELLIPSIS;
        final Color textColor = Color.WHITE;

        /* Requires a ~2 pixels adjustment to be centered on the states */
        final double yOffset = TimeGraphWidget.ENTRY_HEIGHT / 2.0 + 2.0;
        Collection<Node> texts = stateRectangles.stream()
                /* Only try to annotate rectangles that are large enough */
                .filter(stateRect -> stateRect.getWidth() > ellipsisWidth)
                .filter(stateRect -> stateRect.getStateInterval().getLabel() != null)
                .map(stateRect -> {
                    String labelText = requireNonNull(stateRect.getStateInterval().getLabel());
                    /* A small offset looks better here */
                    double textX = Math.max(minX, stateRect.getX()) + 4.0;
                    double textY = stateRect.getY() + yOffset;

                    double rectEndX = stateRect.getX() + stateRect.getWidth();
                    double minWidth = rectEndX - textX;

                    String ellipsedText = JfxUtils.computeClippedText(textFont,
                            labelText,
                            minWidth,
                            overrunStyle,
                            ellipsisStr);

                    if (ellipsedText.equals(ellipsisStr)) {
                        return null;
                    }

                    Text text = new Text(textX, textY, ellipsedText);
                    text.setFont(textFont);
                    text.setFill(textColor);
                    return text;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new Group(texts);
    }

    /**
     * Retrieve the state rectangles currently present in the scenegraph. This
     * should include all currently visible ones, but also possibly more (due to
     * padding, prefetching, etc.)
     *
     * @return The state rectangles
     */
    public Collection<StateRectangle> getRenderedStateRectangles() {
        if (getParentGroup().getChildren().isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        Collection<?> stateRectangles = ((Group) getParentGroup().getChildren().get(0)).getChildren();
        @SuppressWarnings("unchecked")
        Collection<StateRectangle> ret = (@NonNull Collection<StateRectangle>) stateRectangles;
        return ret;
    }

    /**
     * Retrieve the state labels current drawn by this layer.
     *
     * @return The state labels
     */
    public Collection<Text> getRenderedStateLabels() {
        if (fLabelGroup.getChildren().isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        /* The labels are wrapped in a group */
        Collection<?> texts = ((Group) fLabelGroup.getChildren().get(0)).getChildren();
        @SuppressWarnings("unchecked")
        Collection<Text> ret = (@NonNull Collection<Text>) texts;
        return ret;
    }
}
