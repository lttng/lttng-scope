/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope

import com.efficios.jabberwocky.analysis.eventstats.EventStatsXYChartProvider
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.resources.ResourcesCpuIrqModelProvider
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.resources.ResourcesIrqModelProvider
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.threads.ThreadsModelProvider
import com.efficios.jabberwocky.views.timegraph.model.provider.TimeGraphModelProviderManager
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProvider
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProviderManager
import javafx.scene.control.SplitPane
import javafx.scene.layout.Border
import javafx.scene.layout.BorderPane
import org.lttng.scope.views.timecontrol.TimeControl
import org.lttng.scope.views.timeline.TimelineView

/**
 * Main window of LTTng Scope, should be the root node of the main Scene.
 */
class ScopeMainWindow : BorderPane() {

    init {
        /* Load all supported plugins */
        val timeGraphMgr = TimeGraphModelProviderManager.instance();
        timeGraphMgr.registerProviderFactory { ThreadsModelProvider() }
        timeGraphMgr.registerProviderFactory { ResourcesCpuIrqModelProvider() }
        timeGraphMgr.registerProviderFactory { ResourcesIrqModelProvider() }

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

        /* Instantiate the timeline and its manager */
        val timelineView = TimelineView()

        /* The "analysis area" consists of the Timeline pane, and the Time Control at the bottom. */
        val analysisArea = BorderPane()
        analysisArea.center = timelineView.splitPane
        analysisArea.bottom = TimeControl()

        /* The "main pane" separates the project view on the left and the analysis area on the right */
        val projectView = BorderPane() // TODO
        val mainPane = SplitPane(projectView, analysisArea)

        this.center = mainPane
    }

}