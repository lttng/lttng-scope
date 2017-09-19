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
import org.lttng.scope.views.context.ViewGroupContextManager
import org.lttng.scope.views.jfx.JfxUtils

/**
 * Wrapper representing the "timeline" area of the main window.
 *
 * Widgets can be added to the timeline using the addWidget() function.
 * // TODO Make widgets removable
 *
 * The 'splitPane' property is the JavaFX node to add to the scenegraph.
 */
class TimelineView {

    private val itemWeights = mutableMapOf<Node, Int>()

    val splitPane: SplitPane = SplitPane()
    init {
        splitPane.orientation = Orientation.VERTICAL
    }

    private val manager = TimelineManager(this, ViewGroupContextManager.getCurrent())

    fun addWidget(widget: TimelineWidget) {
        JfxUtils.runOnMainThread {
            val node = widget.rootNode
            itemWeights.put(node, widget.weight)

            splitPane.items.add(node)
            splitPane.items.sortBy { itemWeights[it] }
        }
    }

    /**
     * Reset the divider positions of all active widget to their default values.
     * Has to be done *after* the Stage/Scene is initialized.
     */
    fun resetSeparatorPosition() {
        manager.resetInitialSeparatorPosition()
    }

    fun dispose() {
        manager.dispose()
        splitPane.items.clear()
    }

}
