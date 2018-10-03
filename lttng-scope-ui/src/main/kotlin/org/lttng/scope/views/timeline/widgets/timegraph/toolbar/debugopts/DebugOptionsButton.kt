/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar.debugopts;

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import org.lttng.scope.common.jfx.JfxImageFactory
import org.lttng.scope.common.jfx.JfxUtils
import org.lttng.scope.views.timeline.DebugOptions
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget

/**
 * Button to open the debug options dialog.
 *
 * @param widget
 *            The time graph widget to which this toolbar button is
 *            associated.
 */
class DebugOptionsButton(widget: TimeGraphWidget) : Button() {

    companion object {
        private const val CONFIG_ICON_PATH = "/icons/toolbar/config.gif"
    }

    /*
     * Since the button is more persistent than the dialog, track state like the
     * last-selected tab in here.
     */
    val lastSelectedTabProperty: IntegerProperty = SimpleIntegerProperty(0)

    val debugOptions: DebugOptions = widget.debugOptions

    init {
        JfxImageFactory.getImageFromResource(CONFIG_ICON_PATH)
                ?.let { graphic = ImageView(it) }

        tooltip = Tooltip(Messages.debugOptionsDialogName)

        setOnAction {
            DebugOptionsDialog(this).apply {
                show()
                JfxUtils.centerDialogOnScreen(this@apply, this@DebugOptionsButton)
            }
        }
    }
}
