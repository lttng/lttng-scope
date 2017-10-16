/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline

import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.views.timegraph.control.TimeGraphModelControl
import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProviderFactory
import com.efficios.jabberwocky.views.timegraph.model.provider.TimeGraphModelProviderManager
import com.efficios.jabberwocky.views.timegraph.model.provider.TimeGraphModelProviderManager.TimeGraphOutput
import com.efficios.jabberwocky.views.timegraph.view.TimeGraphModelView
import com.efficios.jabberwocky.views.xychart.control.XYChartControl
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProviderManager
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProviderManager.XYChartModelProviderFactory
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProviderManager.XYChartOutput
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import org.lttng.scope.common.NestingBoolean
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget
import org.lttng.scope.views.timeline.widgets.xychart.XYChartFullRangeWidget
import org.lttng.scope.views.timeline.widgets.xychart.XYChartVisibleRangeWidget
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TimelineManager(private val view: TimelineView,
                      private val viewContext: ViewGroupContext) : TimeGraphOutput, XYChartOutput {

    companion object {
        /** Application-wide debug options */
        @JvmField
        val DEBUG_OPTIONS = DebugOptions()

        private const val FULL_RANGE_WIDGET_WEIGHT = 10
        private const val PARTIAL_RANGE_WIDGET_WEIGHT = 20
        private const val TIMEGRAPH_WEIGHT = 30

        private const val INITIAL_DIVIDER_POSITION = 0.15
    }

    private val uiRedrawTimer = Timer()

    private val widgets: MutableSet<TimelineWidget> = ConcurrentHashMap.newKeySet()

    private val hScrollListenerStatus = NestingBoolean()

    private val dividerPosition: DoubleProperty = SimpleDoubleProperty(INITIAL_DIVIDER_POSITION)
    private val hScrollValue: DoubleProperty = SimpleDoubleProperty(0.0)

    /* Properties to sync ongoing selection rectangles */
    private val selectionVisible: BooleanProperty = SimpleBooleanProperty(true)
    private val ongoingSelectionX: DoubleProperty = SimpleDoubleProperty()
    private val ongoingSelectionWidth: DoubleProperty = SimpleDoubleProperty()
    private val ongoingSelectionVisible: BooleanProperty = SimpleBooleanProperty(false)

    init {
        TimeGraphModelProviderManager.instance().registerOutput(this)
        XYChartModelProviderManager.registerOutput(this)

        /* Start the periodic redraw thread */
        val delay = DEBUG_OPTIONS.uiUpdateDelay.get().toLong()
        uiRedrawTimer.schedule(UiRedrawTask(), delay, delay)
    }

    override fun providerRegistered(factory: ITimeGraphModelProviderFactory) {
        /* Instantiate a widget for this provider type */
        val provider = factory.get()
        val control = TimeGraphModelControl(viewContext, provider)
        val viewer = TimeGraphWidget(control, hScrollListenerStatus, TIMEGRAPH_WEIGHT)
        control.attachView(viewer)

        /*
         * Bind properties in a runLater() statement, so that the UI views have
         * already been initialized. The divider position, for instance, only
         * has effect after the view is visible.
         */
        Platform.runLater {
            /* Bind divider position, if applicable */
            viewer.splitPane.dividers[0].positionProperty().bindBidirectional(dividerPosition)

            /* Bind h-scrollbar position */
            viewer.timeBasedScrollPane.hvalueProperty().bindBidirectional(hScrollValue)

            /* Bind the selection rectangles together */
            viewer.selectionRectangle?.apply {
                visibleProperty().bindBidirectional(selectionVisible)
            }

            viewer.ongoingSelectionRectangle?.apply {
                layoutXProperty().bindBidirectional(ongoingSelectionX)
                widthProperty().bindBidirectional(ongoingSelectionWidth)
                visibleProperty().bindBidirectional(ongoingSelectionVisible)
            }
        }

        widgets.add(viewer)
        view.addWidget(viewer)
    }

    override fun providerRegistered(factory: XYChartModelProviderFactory) {
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
        val provider = factory.invoke()

        /* Create the "visible range" widget. */
        val visibleRangecontrol = XYChartControl(viewContext, provider)
        val visibleRangeWidget = XYChartVisibleRangeWidget(visibleRangecontrol, PARTIAL_RANGE_WIDGET_WEIGHT)
        visibleRangecontrol.view = visibleRangeWidget
        widgets.add(visibleRangeWidget)
        view.addWidget(visibleRangeWidget)

        /* Create the "full range" widget. */
        val fullRangeControl = XYChartControl(viewContext, provider)
        val fullRangeWidget = XYChartFullRangeWidget(fullRangeControl, FULL_RANGE_WIDGET_WEIGHT)
        fullRangeControl.view = fullRangeWidget
        widgets.add(fullRangeWidget)
        view.addWidget(fullRangeWidget)

        /* Bind properties accordingly */
        Platform.runLater {
            /* Bind divider position, if applicable */
            val splitPane = visibleRangeWidget.splitPane
            splitPane.dividers[0].positionProperty().bindBidirectional(dividerPosition)

            // TODO Bind selection rectangles once implemented
        }
    }

    fun dispose() {
        TimeGraphModelProviderManager.instance().unregisterOutput(this)

        /* Stop the redraw thread */
        uiRedrawTimer.cancel()
        uiRedrawTimer.purge()

        /* Dispose and clear all the widgets */
        widgets.forEach {
            if (it is TimeGraphModelView) {
                /*
                 * TimeGraphModelView's are disposed via their control
                 *
                 * FIXME Do this better.
                 */
                it.control.dispose()
            } else {
                it.dispose()
            }
        }
        widgets.clear()
    }

    internal fun resetInitialSeparatorPosition() {
        dividerPosition.set(INITIAL_DIVIDER_POSITION)
    }

    private inner class UiRedrawTask : TimerTask() {
        override fun run() {
            /* This update runs in the same thread. */
            widgets.forEach { it.timelineWidgetUpdateTask?.run() }
        }
    }

}
