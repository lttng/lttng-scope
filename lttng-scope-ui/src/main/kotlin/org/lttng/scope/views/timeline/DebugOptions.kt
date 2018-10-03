/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline

import com.efficios.jabberwocky.common.ConfigOption
import javafx.scene.paint.*
import javafx.scene.text.Font
import javafx.scene.text.Text

/**
 * Debug options for the timeline. Advanced users or unit
 * tests might want to modify these.
 */
class DebugOptions {

    companion object {
        const val ELLIPSIS_STRING = "..."
    }

    // ------------------------------------------------------------------------
    // General options
    // ------------------------------------------------------------------------

    /**
     * Painting flag. Indicates if automatic redrawing of the view is enabled
     */
    val isPaintingEnabled = ConfigOption(true)

    /**
     * Entry padding. Number of tree elements to print above *and* below the
     * visible range
     */
    val entryPadding = ConfigOption(5)

    /**
     * How much "padding" around the current visible window, on the left and
     * right, should be pre-rendered. Expressed as a fraction of the current
     * window (for example, 1.0 would render one "page" on each side).
     */
    val renderRangePadding = ConfigOption(0.1)

    /**
     * Time between UI updates, in milliseconds
     */
    val uiUpdateDelay = ConfigOption(250)

    /**
     * Whether the view should respond to vertical or horizontal scrolling
     * actions.
     */
    val isScrollingListenersEnabled = ConfigOption(true)

    // ------------------------------------------------------------------------
    // Loading overlay
    // ------------------------------------------------------------------------

    val isLoadingOverlayEnabled = ConfigOption(true)

    val loadingOverlayColor: ConfigOption<Color> = ConfigOption(Color.GRAY)

    val loadingOverlayFullOpacity = ConfigOption(0.3)
    val loadingOverlayTransparentOpacity = ConfigOption(0.0)

    val loadingOverlayFadeInDuration = ConfigOption(1000.0)
    val loadingOverlayFadeOutDuration = ConfigOption(100.0)

    // ------------------------------------------------------------------------
    // Zoom animation
    // ------------------------------------------------------------------------

    /**
     * The zoom animation duration, which is the amount of milliseconds it takes
     * to complete the zoom animation (smaller number means a faster animation).
     */
    val zoomAnimationDuration = ConfigOption(50)

    /**
     * Each zoom action (typically, one mouse-scroll == one zoom action) will
     * increase or decrease the current visible time range by this factor.
     */
    val zoomStep = ConfigOption(0.08)

    /**
     * Each zoom action will be centered on the center of the selection if it's
     * currently visible.
     */
    val zoomPivotOnSelection = ConfigOption(true)

    /**
     * Each zoom action will be centered on the current mouse position if the
     * zoom action originates from a mouse event. If zoomPivotOnSelection is
     * enabled, it has priority.
     */
    val zoomPivotOnMousePosition = ConfigOption(true)

    // ------------------------------------------------------------------------
    // State rectangles
    // ------------------------------------------------------------------------

    val stateIntervalOpacity = ConfigOption(1.0)

    val multiStatePaint: ConfigOption<Paint> = listOf(Stop(0.0, Color.BLACK), Stop(1.0, Color.WHITE))
        .let { LinearGradient(0.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE, it) }
        .let { ConfigOption(it) }

    // ------------------------------------------------------------------------
    // State labels
    // ------------------------------------------------------------------------

    val stateLabelFont = ConfigOption(Text().font).apply {
        addListener { _ -> ellipsisWidth = recomputeEllipsisWidth() }
    }

    @Transient
    var ellipsisWidth: Double = recomputeEllipsisWidth()
        private set

    @Synchronized
    private fun recomputeEllipsisWidth(): Double {
        with(Text(ELLIPSIS_STRING)) {
            font = stateLabelFont.get()
            applyCss()
            return layoutBounds.width
        }
    }

    // ------------------------------------------------------------------------
    // Tooltips
    // ------------------------------------------------------------------------

    val toolTipFont: ConfigOption<Font> = ConfigOption(Font.font(14.0))

    val toolTipFontFill: ConfigOption<Color> = ConfigOption(Color.WHITE)

}
