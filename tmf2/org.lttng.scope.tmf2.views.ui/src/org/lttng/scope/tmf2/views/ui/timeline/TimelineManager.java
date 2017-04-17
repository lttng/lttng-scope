/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.lttng.scope.tmf2.views.core.context.ViewGroupContext;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProviderFactory;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.TimeGraphModelProviderManager;
import org.lttng.scope.tmf2.views.core.timegraph.view.TimeGraphModelView;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;

import com.google.common.collect.ImmutableSet;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class TimelineManager {

    private static final double INITIAL_DIVIDER_POSITION = 0.2;

    private final Set<ITimelineWidget> fWidgets = new LinkedHashSet<>();

    private final DoubleProperty fDividerPosition = new SimpleDoubleProperty(INITIAL_DIVIDER_POSITION);
    private final DoubleProperty fHScrollValue = new SimpleDoubleProperty(0);

    public TimelineManager(ViewGroupContext viewContext) {

        /* Add widgets for all known timegraph model providers */
        for (ITimeGraphModelProviderFactory factory : TimeGraphModelProviderManager.instance().getRegisteredProviderFactories()) {
            /* Instantiate a widget for this provider type */
            ITimeGraphModelRenderProvider provider = factory.get();
            TimeGraphModelControl control = new TimeGraphModelControl(viewContext, provider);
            TimeGraphWidget viewer = new TimeGraphWidget(control);
            control.attachView(viewer);

            fWidgets.add(viewer);
        }

        /*
         * Bind properties in a runLater() statement, so that the UI views have
         * already been initialized. The divider position, for instance, only
         * has effect after the view is visible.
         */
        Platform.runLater(() -> {
            /* Bind divider positions, where applicable */
            fWidgets.stream()
                    .map(w -> w.getSplitPane())
                    .filter(Objects::nonNull)
                    .map(p -> Objects.requireNonNull(p))
                    .forEach(splitPane -> splitPane.getDividers().get(0).positionProperty().bindBidirectional(fDividerPosition));

            /* Bind h-scrollbar positions */
            fWidgets.stream()
                    .map(w -> w.getTimeBasedScrollPane())
                    .filter(Objects::nonNull)
                    .map(p -> Objects.requireNonNull(p))
                    .forEach(scrollPane -> scrollPane.hvalueProperty().bindBidirectional(fHScrollValue));
        });
    }

    public void dispose() {
        fWidgets.forEach(w -> {
            if (w instanceof TimeGraphModelView) {
                /*
                 * TimeGraphModelView's are disposed via their control
                 *
                 * FIXME Do this better.
                 */
                ((TimeGraphModelView) w).getControl().dispose();
            } else {
                w.dispose();
            }
        });
    }

    public Iterable<ITimelineWidget> getWidgets() {
        return ImmutableSet.copyOf(fWidgets);
    }

    void resetInitialSeparatorPosition() {
        fDividerPosition.set(INITIAL_DIVIDER_POSITION);
    }

}
