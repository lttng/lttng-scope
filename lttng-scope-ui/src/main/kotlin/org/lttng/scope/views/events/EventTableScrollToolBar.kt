/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.events

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.lttng.scope.common.jfx.JfxImageFactory

class EventTableScrollToolBar(tableControl: EventTableControl) : ToolBar() {

    init {
        orientation = Orientation.VERTICAL

        with(tableControl) {
            items.addAll(ScrollToBeginningButton(this),
                    PageUpButton(this),
                    Spacer(),
                    PageDownButton(this),
                    ScrollToEndButton(this))
        }
    }

}

private class ScrollToBeginningButton(tableControl: EventTableControl) : ToolBarButton(
        "Scroll To Beginning",
        "/icons/table/top.png",
        EventHandler { tableControl.scrollToBeginning() })

private class PageUpButton(tableControl: EventTableControl) : ToolBarButton(
        "Scroll Up One Page",
        "/icons/table/pageup.png",
        EventHandler { tableControl.pageUp() })

private class PageDownButton(tableControl: EventTableControl) : ToolBarButton(
        "Scroll Down One Page",
        "/icons/table/pagedown.png",
        EventHandler { tableControl.pageDown() })

private class ScrollToEndButton(tableControl: EventTableControl) : ToolBarButton(
        "Scroll To End",
        "/icons/table/bottom.png",
        EventHandler { tableControl.scrollToEnd() })

private abstract class ToolBarButton(buttonTooltip: String,
                                     buttonIconPath: String,
                                     action: EventHandler<ActionEvent>) : Button() {

    init {
        val icon = JfxImageFactory.getImageFromResource(buttonIconPath)
        graphic = ImageView(icon)
        tooltip = Tooltip(buttonTooltip)
        onAction = action
    }
}

private class Spacer : Pane() {
    init {
        VBox.setVgrow(this, Priority.ALWAYS)
    }
}