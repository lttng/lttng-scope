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
import javafx.scene.Node
import javafx.scene.control.*
import org.lttng.scope.application.actions.openTraceAction
import org.lttng.scope.common.TimestampFormat
import org.lttng.scope.views.context.ViewGroupContextManager

class ScopeMenuBar : MenuBar() {
    init {
        menus.addAll(FileMenu(this),
                ViewMenu()
//                HelpMenu()
        )
    }
}


private class FileMenu(private val refNode: Node) : Menu(FILE_MENU) {

    companion object {
        private const val FILE_MENU = "File"
        private const val OPEN_ACTION = "Open..."
        private const val CLOSE_ACTION = "Close Current Project"
        private const val EXIT_ACTION = "Exit"
    }

    private val openMenuItem = MenuItem(OPEN_ACTION).apply {
        setOnAction { openTraceAction(refNode) }
    }
    private val closeMenuItem = MenuItem(CLOSE_ACTION).apply {
        setOnAction { ViewGroupContextManager.getCurrent().switchProject(null) }
    }
    private val exitMenuItem = MenuItem(EXIT_ACTION).apply {
        setOnAction { refNode.scene.window.hide() }
    }

    init {
        items.addAll(openMenuItem,
                closeMenuItem,
                SeparatorMenuItem(),
                exitMenuItem)
    }
}


private class ViewMenu : Menu(VIEW_MENU) {

    companion object {
        private const val VIEW_MENU = "View"
        private const val TIMESTAMP_FORMAT_HEADER = "Timestamp Formatting"
        private const val TIMESTAMP_FORMAT_OPTION_FULL_DATE_TIMEZONE = "YYYY-MM-DD hh:mm:ss.n TZ"
        private const val TIMESTAMP_FORMAT_OPTION_FULL_DATE = "YYYY-MM-DD hh:mm:ss.n"
        private const val TIMESTAMP_FORMAT_OPTION_HMS_NANOS = "hh:mm:ss.n"
        private const val TIMESTAMP_FORMAT_OPTION_SECONDS_NANOS = "s.n"

        private const val TIMEZONE_TO_USE_HEADER = "Time Zone"
        private const val TIMEZONE_TO_USE_LOCAL = "Local"
        private const val TIMEZONE_TO_USE_UTC = "UTC"
    }

    private val timestampFormatRMIs = arrayOf(
            RadioMenuItem(TIMESTAMP_FORMAT_OPTION_FULL_DATE_TIMEZONE).apply {
                setOnAction { ScopeOptions.timestampFormat = TimestampFormat.YMD_HMS_N_TZ }
            },
            RadioMenuItem(TIMESTAMP_FORMAT_OPTION_FULL_DATE).apply {
                setOnAction { ScopeOptions.timestampFormat = TimestampFormat.YMD_HMS_N }
            },
            RadioMenuItem(TIMESTAMP_FORMAT_OPTION_HMS_NANOS).apply {
                setOnAction { ScopeOptions.timestampFormat = TimestampFormat.HMS_N }
            },
            RadioMenuItem(TIMESTAMP_FORMAT_OPTION_SECONDS_NANOS).apply {
                setOnAction { ScopeOptions.timestampFormat = TimestampFormat.SECONDS_POINT_NANOS }
            })

    /** Listener to update the displayed entry if the option changes elsewhere. */
    private val timestampFormatChangeListener = ChangeListener<TimestampFormat> { _, _, newValue ->
        newValue ?: return@ChangeListener
        when (newValue) {
            TimestampFormat.YMD_HMS_N_TZ -> timestampFormatRMIs[0].isSelected = true
            TimestampFormat.YMD_HMS_N -> timestampFormatRMIs[1].isSelected = true
            TimestampFormat.HMS_N -> timestampFormatRMIs[2].isSelected = true
            TimestampFormat.SECONDS_POINT_NANOS -> timestampFormatRMIs[3].isSelected = true
        }
    }

    private val timeZoneRMIs = arrayOf(
            RadioMenuItem(TIMEZONE_TO_USE_LOCAL).apply {
                setOnAction { ScopeOptions.timestampTimeZone = ScopeOptions.DisplayTimeZone.LOCAL }
            },
            RadioMenuItem(TIMEZONE_TO_USE_UTC).apply {
                setOnAction { ScopeOptions.timestampTimeZone = ScopeOptions.DisplayTimeZone.UTC }
            }
    )

    private val timeZoneToUseChangeListener = ChangeListener<ScopeOptions.DisplayTimeZone> { _, _, newValue ->
        newValue ?: return@ChangeListener
        when (newValue) {
            ScopeOptions.DisplayTimeZone.LOCAL -> timeZoneRMIs[0].isSelected = true
            ScopeOptions.DisplayTimeZone.UTC -> timeZoneRMIs[1].isSelected = true
        }
    }

    init {
        val timestampFormatHeaderItem = MenuItem(TIMESTAMP_FORMAT_HEADER).apply {
            isDisable = true
        }

        val timestampFormatToggleGroup = ToggleGroup()
        timestampFormatRMIs.forEach { it.toggleGroup = timestampFormatToggleGroup }

        /* "Fire" the listener manually initially to set the initial state. */
        timestampFormatChangeListener.changed(null, null, ScopeOptions.timestampFormat)
        /* then attach the listener to the property to track future changes */
        ScopeOptions.timestampFormatProperty().addListener(timestampFormatChangeListener)

        val timeZoneToUseHeaderItem = MenuItem(TIMEZONE_TO_USE_HEADER).apply {
            isDisable = true
        }

        val timeZoneToggleGroup = ToggleGroup()
        timeZoneRMIs.forEach { it.toggleGroup = timeZoneToggleGroup }
        timeZoneToUseChangeListener.changed(null, null, ScopeOptions.timestampTimeZone)
        ScopeOptions.timestampTimeZoneProperty().addListener(timeZoneToUseChangeListener)

        items.addAll(timestampFormatHeaderItem, *timestampFormatRMIs,
                SeparatorMenuItem(),
                timeZoneToUseHeaderItem, *timeZoneRMIs)
    }

}


private class HelpMenu : Menu(HELP_MENU) {

    companion object {
        private const val HELP_MENU = "Help"
        private const val ABOUT_ACTION = "About..."
    }

    private val aboutMenuItem = MenuItem(ABOUT_ACTION).apply {
        // TODO Open an About window...
        // setOnAction { ... }
    }

    init {
        items.addAll(aboutMenuItem)
    }
}
