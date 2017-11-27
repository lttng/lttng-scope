/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.layer.drawnevents;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.views.common.EventSymbolStyle;
import com.efficios.jabberwocky.views.timegraph.model.provider.drawnevents.TimeGraphDrawnEventProvider;
import com.efficios.jabberwocky.views.timegraph.model.provider.drawnevents.TimeGraphDrawnEventProviderManager;
import com.efficios.jabberwocky.views.timegraph.model.render.TimeGraphEvent;
import com.efficios.jabberwocky.views.timegraph.model.render.drawnevents.TimeGraphDrawnEvent;
import com.efficios.jabberwocky.views.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.jetbrains.annotations.Nullable;
import org.lttng.scope.project.filter.SymbolsKt;
import org.lttng.scope.views.jfx.JfxColorFactory;
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget;
import org.lttng.scope.views.timeline.widgets.timegraph.VerticalPosition;
import org.lttng.scope.views.timeline.widgets.timegraph.layer.TimeGraphLayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class TimeGraphDrawnEventLayer extends TimeGraphLayer {

    private final DrawnEventFilterListener filterListener;
    private final Map<TimeGraphDrawnEventProvider, Group> fEventProviders = new HashMap<>();

    public TimeGraphDrawnEventLayer(TimeGraphWidget widget, Group parentGroup) {
        super(widget, parentGroup);

        ObservableSet<TimeGraphDrawnEventProvider> providers = TimeGraphDrawnEventProviderManager.instance().getRegisteredProviders();
        /* Populate with the initial values */
        providers.forEach(this::trackEventProvider);

        /* Add listeners to track registered/deregistered providers */
        providers.addListener((SetChangeListener<TimeGraphDrawnEventProvider>) change -> {
            if (change == null) {
                return;
            }
            TimeGraphDrawnEventProvider addedProvider = change.getElementAdded();
            if (addedProvider != null) {
                trackEventProvider(addedProvider);
            }

            TimeGraphDrawnEventProvider removedProvider = change.getElementRemoved();
            if (removedProvider != null) {
                untrackEventProvider(removedProvider);
            }
        });

        filterListener = new DrawnEventFilterListener(getWidget());
    }

    private void trackEventProvider(TimeGraphDrawnEventProvider provider) {
        Group newGroup = new Group();
        Group oldGroup = fEventProviders.put(provider, newGroup);
        if (oldGroup == null) {
            Platform.runLater(() -> {
                getParentGroup().getChildren().add(newGroup);
            });
        } else {
            /* Remove the old group in case there was already one. */
            Platform.runLater(() -> {
                getParentGroup().getChildren().remove(oldGroup);
                getParentGroup().getChildren().add(newGroup);
            });
        }

        /*
         * Add a listener to this provider's "enabled" property, so that when it
         * changes from enabled to disabled and vice versa, we update the view
         * accordingly.
         */
        provider.enabledProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                /* The provider was just enabled */
                TimeRange timeRange = getWidget().getViewContext().getCurrentVisibleTimeRange();
                TimeGraphTreeRender treeRender = getWidget().getLatestTreeRender();
                // FIXME Use a Task?
                paintEventsOfProvider(treeRender, timeRange, provider, null);
            } else {
                /* Provider was disabled. Clear the children of its group. */
                Group group = fEventProviders.get(provider);
                if (group == null) {
                    return;
                }
                Platform.runLater(() -> group.getChildren().clear());
            }
        });

    }

    private void untrackEventProvider(TimeGraphDrawnEventProvider provider) {
        Group group = fEventProviders.remove(provider);
        if (group != null) {
            Platform.runLater(() -> {
                getParentGroup().getChildren().remove(group);
            });
        }
        /*
         * Note, we do not need to explicitly remove the ChangeListener we added
         * above, the weak reference will simply lose it at some point.
         */
    }

    @Override
    public void drawContents(TimeGraphTreeRender treeRender, TimeRange timeRange,
            VerticalPosition vPos, @Nullable FutureTask<?> task) {
        fEventProviders.keySet().stream()
                .filter(provider -> provider.enabledProperty().get())
                .forEach(provider -> paintEventsOfProvider(treeRender, timeRange, provider, task));
    }

    @Override
    public void clear() {
        fEventProviders.values().forEach(group -> group.getChildren().clear());
    }

    private void paintEventsOfProvider(TimeGraphTreeRender treeRender, TimeRange timeRange,
            TimeGraphDrawnEventProvider eventsProvider, @Nullable FutureTask<?> task) {

        TimeGraphDrawnEventRender eventRender = eventsProvider.getEventRender(treeRender, timeRange, task);
        Collection<Shape> drawnEvents = prepareDrawnEvents(treeRender, eventRender);

        Group paintGroup = requireNonNull(fEventProviders.get(eventsProvider));
        Platform.runLater(() -> {
            paintGroup.getChildren().clear();
            paintGroup.getChildren().addAll(drawnEvents);
        });
    }

    private Collection<Shape> prepareDrawnEvents(TimeGraphTreeRender treeRender, TimeGraphDrawnEventRender eventRender) {
        final double entryHeight = TimeGraphWidget.ENTRY_HEIGHT;

        Collection<Shape> shapes = eventRender.getEvents().stream()
                .map(event -> {
                    TimeGraphEvent tgEvent = event.getEvent();
                    double x = getWidget().timestampToPaneXPos(tgEvent.getTimestamp());

                    int treeIndex = treeRender.getAllTreeElements().indexOf(tgEvent.getTreeElement());
                    if (treeIndex == -1) {
                        return null;
                    }
                    double y = treeIndex * entryHeight + entryHeight / 2;

                    Shape shape = getShapeFromEvent(event);
                    /*
                     * Some symbols already use the layout* properties for
                     * adjusting their center. Use translate* properties for
                     * their positioning on the timegraph.
                     */
                    shape.setTranslateX(x);
                    shape.setTranslateY(y);
                    return shape;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return shapes;
    }

    private static Shape getShapeFromEvent(TimeGraphDrawnEvent event) {
        Color color = JfxColorFactory.getColorFromDef(event.getEventSeries().getColor().get());
        EventSymbolStyle symbol = event.getEventSeries().getSymbolStyle().get();
        return SymbolsKt.getGraphic(symbol, new ReadOnlyObjectWrapper<>(color));
    }

    public synchronized Collection<Shape> getRenderedEvents() {
        /*
         * Retrieve the rendered events of each group, and flatten them into a
         * single collection.
         */
        return fEventProviders.values().stream()
                .map(Group::getChildren)
                .flatMap(Collection::stream)
                .map(node -> (Shape) node)
                .collect(Collectors.toList());
    }

}
