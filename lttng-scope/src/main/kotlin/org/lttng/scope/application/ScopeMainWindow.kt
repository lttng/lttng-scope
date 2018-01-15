/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application

import com.efficios.jabberwocky.analysis.eventstats.EventStatsXYChartProvider
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.resources.ResourcesCpuModelProvider
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.threads.ThreadsModelProvider
import com.efficios.jabberwocky.views.timegraph.model.provider.TimeGraphModelProviderManager
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProvider
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProviderManager
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import org.lttng.scope.application.task.ScopeStatusBar
import org.lttng.scope.project.ProjectArea
import org.lttng.scope.views.context.ViewGroupContextManager
import org.lttng.scope.views.events.EventTableControl
import org.lttng.scope.views.timecontrol.TimeControl
import org.lttng.scope.views.timeline.TimelineView

private const val INITIAL_DIVIDER_POSITION = 0.15

/**
 * Main window of LTTng Scope, should be the root node of the main Scene.
 */
class ScopeMainWindow : BorderPane() {

    /** The "main pane" separates the project view on the left and the analysis area on the right */
    private val mainPane: SplitPane

    /** The project area is where the trace project trees are shown. */
    private val projectArea: ProjectArea

    /** The "analysis area" consists of the Timeline pane, and the Time Control at the bottom. */
    private class AnalysisArea : VBox() {

        val timelineView = TimelineView()
        val eventTableControl = EventTableControl(ViewGroupContextManager.getCurrent())

        /** Area containing the timeline widgets and the event table */
        val widgetArea = SplitPane(timelineView.rootNode, eventTableControl.table)

        init {
            widgetArea.orientation = Orientation.VERTICAL
            children.addAll(widgetArea, TimeControl())
        }
    }

    private val analysisArea: AnalysisArea

    init {
        /* Load all supported plugins */
        val timeGraphMgr = TimeGraphModelProviderManager.instance()
        timeGraphMgr.registerProviderFactory { ThreadsModelProvider() }
        timeGraphMgr.registerProviderFactory { ResourcesCpuModelProvider() }

        val xyChartMgr = XYChartModelProviderManager
        /*
         * No SAM-conversion for Kotlin types :(
         * See https://youtrack.jetbrains.com/issue/KT-7770
         */
        val factory1 = object : XYChartModelProviderManager.XYChartModelProviderFactory {
            override fun invoke(): XYChartModelProvider = EventStatsXYChartProvider()
        }
        xyChartMgr.registerProviderFactory(factory1)

        /* Register the built-in LTTng-Analyses descriptors */
//        try {
//            LttngAnalysesLoader.load();
//        } catch (LamiAnalysisFactoryException | IOException e) {
//            // Not the end of the world if the analyses are not available
//            logWarning("Cannot find LTTng analyses configuration files: " + e.getMessage()); //$NON-NLS-1$
//        }

        val menuBar = ScopeMenuBar()
        val statusBar = ScopeStatusBar()

        analysisArea = AnalysisArea()
        projectArea = ProjectArea()
        mainPane = SplitPane(projectArea, analysisArea)

        this.top = menuBar
        this.center = mainPane
        this.bottom = statusBar

        addEventHandler(KeyEvent.KEY_PRESSED, ScopeKeyBindings.VisibleTimeRangeMovingHandler())
    }

    fun onShownCB() {
        mainPane.setDividerPositions(INITIAL_DIVIDER_POSITION)
        analysisArea.widgetArea.setDividerPositions(0.75)
        with(analysisArea.timelineView) {
            resetTimeBasedSeparatorPosition()
            resizeWidgets()
        }
    }

}