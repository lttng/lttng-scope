/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.arrows.ITimeGraphModelArrowProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;
import org.lttng.scope.tmf2.views.ui.jfx.Arrow;

import com.google.common.collect.ImmutableMap;

import javafx.application.Platform;
import javafx.scene.Group;

public class TimeGraphArrowControl {

    private final LatestTaskExecutor fTaskExecutor = new LatestTaskExecutor();

    private final TimeGraphWidget fWidget;
    private final Map<ITimeGraphModelArrowProvider, Group> fArrowGroups;

    public TimeGraphArrowControl(TimeGraphWidget widget, Group paintTarget) {
        fWidget = widget;
        Collection<ITimeGraphModelArrowProvider> arrowProviders =
                widget.getControl().getModelRenderProvider().getArrowProviders();
        fArrowGroups = arrowProviders.stream()
                .collect(ImmutableMap.toImmutableMap(Function.identity(), e -> new Group()));

        fArrowGroups.values().forEach(paintTarget.getChildren()::add);

        /*
         * Add listeners to the registered arrow providers. If providers become
         * enabled or disabled, we must repaint or hide the arrows from this
         * provider's series.
         */
        arrowProviders.forEach(ap -> {
            ap.enabledProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue) {
                    /*
                     * The provider is now enabled, we must fetch and display
                     * its arrows
                     */
                    TimeRange timeRange = fWidget.getViewContext().getCurrentVisibleTimeRange();
                    TimeGraphTreeRender treeRender = fWidget.getLatestTreeRender();
                    paintArrowsOfProvider(treeRender, timeRange, ap);
                } else {
                    /*
                     * The provider is now disabled, we must remove the existing
                     * arrows from this provider.
                     */
                    Group group = fArrowGroups.get(ap);
                    if (group == null) {
                        return;
                    }
                    Platform.runLater(() -> {
                        group.getChildren().clear();
                    });
                }
            });
        });
    }

    public void paintArrows(TimeGraphTreeRender treeRender, TimeRange timeRange) {
        fArrowGroups.keySet().stream()
                .filter(arrowProvider -> arrowProvider.enabledProperty().get())
                .forEach(arrowProvider -> paintArrowsOfProvider(treeRender, timeRange, arrowProvider));
    }

    public void clear() {
        fArrowGroups.values().forEach(group -> group.getChildren().clear());
    }

    private void paintArrowsOfProvider(TimeGraphTreeRender treeRender, TimeRange timeRange,
            ITimeGraphModelArrowProvider arrowProvider) {
        Group targetGroup = fArrowGroups.get(arrowProvider);
        if (targetGroup == null) {
            /* Should not happen... */
            return;
        }

        TimeGraphArrowRender arrowRender = arrowProvider.getArrowRender(treeRender, timeRange);
        Collection<Arrow> arrows = prepareArrows(treeRender, arrowRender);

        Platform.runLater(() -> {
            targetGroup.getChildren().clear();
            targetGroup.getChildren().addAll(arrows);
        });
    }

    private Collection<Arrow> prepareArrows(TimeGraphTreeRender treeRender,
            TimeGraphArrowRender arrowRender) {
        final double entryHeight = TimeGraphWidget.ENTRY_HEIGHT;

        Collection<Arrow> arrows = arrowRender.getArrows().stream()
            .map(timeGraphArrow -> {
                TimeGraphTreeElement startTreeElem = timeGraphArrow.getStartEvent().getTreeElement();
                TimeGraphTreeElement endTreeElem = timeGraphArrow.getEndEvent().getTreeElement();
                long startTimestamp = timeGraphArrow.getStartEvent().getTimestamp();
                long endTimestamp = timeGraphArrow.getEndEvent().getTimestamp();
                // FIXME Build and use a hashmap instead for indexes
                int startIndex = treeRender.getAllTreeElements().indexOf(startTreeElem);
                int endIndex = treeRender.getAllTreeElements().indexOf(endTreeElem);
                if (startIndex == -1 || endIndex == -1) {
                    /* We shouldn't have received this... */
                    return null;
                }

                double startX = fWidget.timestampToPaneXPos(startTimestamp);
                double endX = fWidget.timestampToPaneXPos(endTimestamp);
                double startY = startIndex * entryHeight + entryHeight / 2;
                double endY = endIndex * entryHeight + entryHeight / 2;

                return new Arrow(startX, startY, endX, endY);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return arrows;
    }

}
