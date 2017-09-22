/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline

import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import org.lttng.scope.views.context.ViewGroupContextManager
import org.lttng.scope.views.jfx.JfxUtils

/**
 * Wrapper representing the "timeline" area of the main window.
 *
 * Widgets can be added to the timeline using the addWidget() function.
 * // TODO Make widgets removable
 *
 * The 'rootNode' property is the JavaFX node to add to the scenegraph.
 */
class TimelineView {

    private val itemWeights = mutableMapOf<Node, Int>()

    /** The middle area, with all the timegraphs, charts, etc. */
    private val splitPane: SplitPane = SplitPane()
    init {
        splitPane.orientation = Orientation.VERTICAL
    }

    /** The little bar at the top of the timeline to display the whole trace range. */
    private val navigationArea = VBox()

    val rootNode = BorderPane()
    init {
        rootNode.top = navigationArea
        rootNode.center = splitPane
    }

    private val manager = TimelineManager(this, ViewGroupContextManager.getCurrent())

    fun addWidget(widget: TimelineWidget) {
        JfxUtils.runOnMainThread {
            val node = widget.rootNode
            if (widget is NavigationAreaWidget) {
                navigationArea.children.add(node)
            } else {
                itemWeights.put(node, widget.weight)
                splitPane.items.add(node)
                splitPane.items.sortBy { itemWeights[it] }
                resizeWidgets()
            }
        }
    }


    /**
     * Reset the time-based (vertical) divider positions of all active widget to their default values.
     * Has to be done *after* the Stage/Scene is initialized.
     */
    fun resetTimeBasedSeparatorPosition() {
        manager.resetInitialSeparatorPosition()
    }

    /**
     * Re-balance the visible widgets, giving them the same vertical size. This corresponds to setting
     * the horizontal divider positions.
     */
    fun resizeWidgets() {
        val nbWidgets = splitPane.items.count()
        if (nbWidgets <= 1) return
        val separatorValue = 1.0 / nbWidgets
        val nbSeparators = nbWidgets - 1
        val values = DoubleArray(nbSeparators, { idx -> (idx + 1) * separatorValue })
        splitPane.setDividerPositions(*values)
    }

    fun dispose() {
        manager.dispose()
        splitPane.items.clear()
    }

}
