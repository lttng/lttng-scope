/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application

import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem

private const val FILE_MENU = "File"
private const val OPEN_ACTION = "Open..."
private const val EXIT_ACTION = "Exit"

private const val HELP_MENU = "Help"
private const val ABOUT_ACTION = "About..."

class ScopeMenuBar : MenuBar() {

    init {
        val openMenuItem = MenuItem(OPEN_ACTION)
        openMenuItem.setOnAction { openTraceAction(this) }
        val exitMenuItem = MenuItem(EXIT_ACTION)
        exitMenuItem.setOnAction { scene.window.hide() }

        val fileMenu = Menu(FILE_MENU)
        fileMenu.items.addAll(openMenuItem,
                SeparatorMenuItem(),
                exitMenuItem)

        val aboutMenuItem = MenuItem(ABOUT_ACTION)
        val helpMenu = Menu(HELP_MENU)
        helpMenu.items.addAll(aboutMenuItem)

        menus.addAll(fileMenu, helpMenu)
    }

}

