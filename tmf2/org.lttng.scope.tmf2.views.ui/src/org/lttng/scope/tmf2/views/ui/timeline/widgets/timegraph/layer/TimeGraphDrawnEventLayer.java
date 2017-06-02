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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.drawnevents.ITimeGraphDrawnEventProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.drawnevents.TimeGraphDrawnEventProviderManager;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.TimeGraphEvent;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEvent;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventSeries.SymbolStyle;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;
import org.lttng.scope.tmf2.views.ui.jfx.JfxColorFactory;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.VerticalPosition;

import javafx.application.Platform;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

public class TimeGraphDrawnEventLayer extends TimeGraphLayer {

    private final Map<ITimeGraphDrawnEventProvider, Group> fEventProviders = new HashMap<>();

    public TimeGraphDrawnEventLayer(TimeGraphWidget widget, Group parentGroup) {
        super(widget, parentGroup);

        ObservableSet<ITimeGraphDrawnEventProvider> providers = TimeGraphDrawnEventProviderManager.instance().getRegisteredProviders();
        /* Populate with the initial values */
        providers.forEach(this::trackEventProvider);

        /* Add listeners to track registered/deregistered providers */
        providers.addListener((SetChangeListener<ITimeGraphDrawnEventProvider>) change -> {
            if (change == null) {
                return;
            }
            ITimeGraphDrawnEventProvider addedProvider = change.getElementAdded();
            if (addedProvider != null) {
                trackEventProvider(addedProvider);
            }

            ITimeGraphDrawnEventProvider removedProvider = change.getElementRemoved();
            if (removedProvider != null) {
                untrackEventProvider(removedProvider);
            }
        });
    }

    private void trackEventProvider(ITimeGraphDrawnEventProvider provider) {
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

    private void untrackEventProvider(ITimeGraphDrawnEventProvider provider) {
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
            ITimeGraphDrawnEventProvider eventsProvider, @Nullable FutureTask<?> task) {

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
        SymbolStyle symbol = event.getEventSeries().getSymbolStyle().get();
        Shape shape = getShapeFromSymbol(symbol);
        shape.setFill(color);
        return shape;
    }

    public static Shape getShapeFromSymbol(SymbolStyle symbol) {
        Shape shape;
        switch (symbol) {
        case CIRCLE:
            shape = new Circle(5);
            break;

        case DIAMOND: {
            shape = new Polygon(5.0, 0.0,
                    10.0, 5.0,
                    5.0, 10.0,
                    0.0, 5.0);
            shape.relocate(-5.0, -5.0);
        }
            break;

        case SQUARE:
            shape = new Rectangle(-5, -5, 10, 10);
            break;

        case STAR:
            // FIXME bigger?
            shape = new Polygon(4.0, 0.0,
                    5.0, 4.0,
                    8.0, 4.0,
                    6.0, 6.0,
                    7.0, 9.0,
                    4.0, 7.0,
                    1.0, 9.0,
                    2.0, 6.0,
                    0.0, 4.0,
                    3.0, 4.0);
            shape.relocate(-4, -4.5);
            break;

        case TRIANGLE: {
            SVGPath path = new SVGPath();
            path.setContent("M5,0 L10,8 L0,8 Z"); //$NON-NLS-1$
            path.relocate(-5, -2);
            shape = path;
        }
            break;

        case CROSS:
        default: {
            SVGPath path = new SVGPath();
            path.setContent("M2,0 L5,4 L8,0 L10,0 L10,2 L6,5 L10,8 L10,10 L8,10 L5,6 L2, 10 L0,10 L0,8 L4,5 L0,2 L0,0 Z"); //$NON-NLS-1$
            path.relocate(-5, -5);
            shape = path;
        }
            break;

        }

        shape.setStroke(Color.BLACK);
        return shape;
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
