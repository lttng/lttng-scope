/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application

import javafx.beans.value.ChangeListener
import javafx.scene.control.*
import org.lttng.scope.application.actions.openTraceAction
import org.lttng.scope.common.TimestampFormat
import org.lttng.scope.views.context.ViewGroupContextManager

private const val FILE_MENU = "File"
private const val OPEN_ACTION = "Open..."
private const val CLOSE_ACTION = "Close Current Project"
private const val EXIT_ACTION = "Exit"

private const val VIEW_MENU = "View"
private const val TIMESTAMP_FORMAT_HEADER = "Timestamp Formatting"
private const val TIMESTAMP_FORMAT_OPTION_FULL_DATE = "YYYY-MM-DD hh:mm:ss.n"
private const val TIMESTAMP_FORMAT_OPTION_HMS_NANOS = "hh:mm:ss.n"
private const val TIMESTAMP_FORMAT_OPTION_SECONDS_NANOS = "s.n"

private const val HELP_MENU = "Help"
private const val ABOUT_ACTION = "About..."

class ScopeMenuBar : MenuBar() {

    init {
        /* "File" menu */
        val openMenuItem = MenuItem(OPEN_ACTION).apply {
            setOnAction { openTraceAction(this@ScopeMenuBar) }
        }
        val closeMenuItem = MenuItem(CLOSE_ACTION).apply {
            setOnAction { ViewGroupContextManager.getCurrent().switchProject(null) }
        }
        val exitMenuItem = MenuItem(EXIT_ACTION).apply {
            setOnAction { scene.window.hide() }
        }
        val fileMenu = Menu(FILE_MENU).apply {
            items.addAll(openMenuItem,
                    closeMenuItem,
                    SeparatorMenuItem(),
                    exitMenuItem)
        }

        /* "Help" menu */
        val aboutMenuItem = MenuItem(ABOUT_ACTION).apply {
            // TODO Open an About window...
            // setOnAction { ... }
        }
        val helpMenu = Menu(HELP_MENU).apply {
            items.addAll(aboutMenuItem)
        }

        menus.addAll(fileMenu, ViewMenu(), helpMenu)
    }

    private class ViewMenu : Menu(VIEW_MENU) {

        private val rmi1 = RadioMenuItem(TIMESTAMP_FORMAT_OPTION_FULL_DATE).apply {
            setOnAction { ScopeOptions.timestampFormat = TimestampFormat.YMD_HMS_N }
        }
        private val rmi2 = RadioMenuItem(TIMESTAMP_FORMAT_OPTION_HMS_NANOS).apply {
            setOnAction { ScopeOptions.timestampFormat = TimestampFormat.HMS_N }
        }
        private val rmi3 = RadioMenuItem(TIMESTAMP_FORMAT_OPTION_SECONDS_NANOS).apply {
            setOnAction { ScopeOptions.timestampFormat = TimestampFormat.SECONDS_POINT_NANOS }
        }

        /** Listener to update the displayed entry if the option changes elsewhere. */
        private val timestampFormatChangeListener = ChangeListener<TimestampFormat> { _, _, newValue ->
            when (newValue) {
                TimestampFormat.YMD_HMS_N -> rmi1.isSelected = true
                TimestampFormat.HMS_N -> rmi2.isSelected = true
                TimestampFormat.SECONDS_POINT_NANOS -> rmi3.isSelected = true
                null -> {}
            }
        }

        init {
            val timestampFormatHeaderItem = MenuItem(TIMESTAMP_FORMAT_HEADER).apply {
                isDisable = true
            }

            val timestampFormatToggleGroup = ToggleGroup()
            listOf(rmi1, rmi2, rmi3).forEach { it.toggleGroup = timestampFormatToggleGroup }

            /* "Fire" the listener manually initially to set the initial state. */
            timestampFormatChangeListener.changed(null, null, ScopeOptions.timestampFormat)
            /* then attach the listener to the property to track future changes */
            ScopeOptions.timestampFormatProperty().addListener(timestampFormatChangeListener)

            items.addAll(timestampFormatHeaderItem, rmi1, rmi2, rmi3)
        }

    }

}

