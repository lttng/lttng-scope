/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.timeline;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.lttng.scope.common.core.NestingBoolean;
import org.lttng.scope.ui.timeline.TimelineWidget.TimelineWidgetUpdateTask;
import org.lttng.scope.ui.timeline.widgets.timegraph.TimeGraphWidget;
import org.lttng.scope.ui.timeline.widgets.xychart.XYChartFullRangeWidget;
import org.lttng.scope.ui.timeline.widgets.xychart.XYChartVisibleRangeWidget;

import com.efficios.jabberwocky.context.ViewGroupContext;
import com.efficios.jabberwocky.views.timegraph.control.TimeGraphModelControl;
import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProvider;
import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProviderFactory;
import com.efficios.jabberwocky.views.timegraph.model.provider.TimeGraphModelProviderManager;
import com.efficios.jabberwocky.views.timegraph.model.provider.TimeGraphModelProviderManager.TimeGraphOutput;
import com.efficios.jabberwocky.views.timegraph.view.TimeGraphModelView;
import com.efficios.jabberwocky.views.xychart.control.XYChartControl;
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProvider;
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProviderManager;
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProviderManager.XYChartModelProviderFactory;
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProviderManager.XYChartOutput;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.shape.Rectangle;

public class TimelineManager implements TimeGraphOutput, XYChartOutput {

    /* Application-wide debug options */
    public static final DebugOptions DEBUG_OPTIONS = new DebugOptions();

    private static final double INITIAL_DIVIDER_POSITION = 0.2;

    private final Timer fUiRedrawTimer = new Timer();

    private final TimelineView fView;
    private final ViewGroupContext fViewContext;
    private final Set<TimelineWidget> fWidgets = ConcurrentHashMap.newKeySet();

    private final NestingBoolean fHScrollListenerStatus = new NestingBoolean();

    private final DoubleProperty fDividerPosition = new SimpleDoubleProperty(INITIAL_DIVIDER_POSITION);
    private final DoubleProperty fHScrollValue = new SimpleDoubleProperty(0);

    /* Properties to sync ongoing selection rectangles */
    private final BooleanProperty fSelectionVisible = new SimpleBooleanProperty(true);
    private final DoubleProperty fOngoingSelectionX = new SimpleDoubleProperty();
    private final DoubleProperty fOngoingSelectionWidth = new SimpleDoubleProperty();
    private final BooleanProperty fOngoingSelectionVisible = new SimpleBooleanProperty(false);

    public TimelineManager(TimelineView view, ViewGroupContext viewContext) {
        fView = view;
        fViewContext = viewContext;
        TimeGraphModelProviderManager.instance().registerOutput(this);
        XYChartModelProviderManager.INSTANCE.registerOutput(this);

        /* Start the periodic redraw thread */
        long delay = DEBUG_OPTIONS.uiUpdateDelay.get();
        fUiRedrawTimer.schedule(new UiRedrawTask(), delay, delay);
    }

    @Override
    public void providerRegistered(ITimeGraphModelProviderFactory factory) {
        /* Instantiate a widget for this provider type */
        ITimeGraphModelProvider provider = factory.get();
        TimeGraphModelControl control = new TimeGraphModelControl(fViewContext, provider);
        TimeGraphWidget viewer = new TimeGraphWidget(control, fHScrollListenerStatus);
        control.attachView(viewer);

        /*
         * Bind properties in a runLater() statement, so that the UI views have
         * already been initialized. The divider position, for instance, only
         * has effect after the view is visible.
         */
        Platform.runLater(() -> {
            /* Bind divider position, if applicable */
            SplitPane splitPane = viewer.getSplitPane();
            splitPane.getDividers().get(0).positionProperty().bindBidirectional(fDividerPosition);

            /* Bind h-scrollbar position */
            ScrollPane scrollPane = viewer.getTimeBasedScrollPane();
            scrollPane.hvalueProperty().bindBidirectional(fHScrollValue);

            /* Bind the selection rectangles together */
            Rectangle selectionRect = viewer.getSelectionRectangle();
            if (selectionRect != null) {
                selectionRect.visibleProperty().bindBidirectional(fSelectionVisible);
            }
            Rectangle ongoingSelectionRect = viewer.getOngoingSelectionRectangle();
            if (ongoingSelectionRect != null) {
                ongoingSelectionRect.layoutXProperty().bindBidirectional(fOngoingSelectionX);
                ongoingSelectionRect.widthProperty().bindBidirectional(fOngoingSelectionWidth);
                ongoingSelectionRect.visibleProperty().bindBidirectional(fOngoingSelectionVisible);
            }
        });

        fWidgets.add(viewer);
        fView.addWidget(viewer.getRootNode());
    }

    @Override
    public void providerRegistered(@NonNull XYChartModelProviderFactory factory) {
        /*
         * Since XY chart data scales well to very large time ranges (each data point
         * simply represents an aggregate of a larger time range), we will create two
         * widgets for each XY chart provider: a "visible range" one and a full range
         * one.
         *
         * Note we will use the same provider for both widgets, so that setting changes
         * are propagated to both widgets. Each widget needs its own control object
         * though.
         */
        XYChartModelProvider provider = factory.invoke();

        /* Create the "visible range" widget. */
        XYChartControl visibleRangecontrol = new XYChartControl(fViewContext, provider);
        XYChartVisibleRangeWidget visibleRangeWidget = new XYChartVisibleRangeWidget(visibleRangecontrol);
        visibleRangecontrol.setView(visibleRangeWidget);
        fWidgets.add(visibleRangeWidget);
        fView.addWidget(visibleRangeWidget.getRootNode());

        /* Create the "full range" widget. */
        XYChartControl fullRangeControl = new XYChartControl(fViewContext, provider);
        XYChartFullRangeWidget fullRangeWidget = new XYChartFullRangeWidget(fullRangeControl);
        fullRangeControl.setView(fullRangeWidget);
        fWidgets.add(fullRangeWidget);
        fView.addWidget(fullRangeWidget.getRootNode());

        /* Bind properties accordingly */
        Platform.runLater(() -> {
            /* Bind divider position, if applicable */
            SplitPane splitPane = visibleRangeWidget.getSplitPane();
            splitPane.getDividers().get(0).positionProperty().bindBidirectional(fDividerPosition);

            // TODO Bind selection rectangles once implemented
        });
    }

    public void dispose() {
        TimeGraphModelProviderManager.instance().unregisterOutput(this);

        /* Stop the redraw thread */
        fUiRedrawTimer.cancel();
        fUiRedrawTimer.purge();

        /* Dispose and clear all the widgets */
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
        fWidgets.clear();
    }

    void resetInitialSeparatorPosition() {
        fDividerPosition.set(INITIAL_DIVIDER_POSITION);
    }

    private class UiRedrawTask extends TimerTask {
        @Override
        public void run() {
            fWidgets.forEach(widget -> {
                TimelineWidgetUpdateTask task = widget.getTimelineWidgetUpdateTask();
                if (task != null) {
                    /* This update runs in the same thread. */
                    task.run();
                }
            });
        }
    }

}
