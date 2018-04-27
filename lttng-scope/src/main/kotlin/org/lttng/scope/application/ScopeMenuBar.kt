/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application

import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.project.TraceProject
import javafx.beans.value.ChangeListener
import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.control.*
import org.lttng.scope.application.actions.createNewProjectAction
import org.lttng.scope.application.actions.setActiveProject
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

private class ScopeMenuItem(label: String,
                            enableOnlyOnActiveProject: Boolean = false,
                            onAction: (ActionEvent) -> Unit) : MenuItem(label) {

    private val projectChangeListener = if (enableOnlyOnActiveProject) {
        object : ViewGroupContext.ProjectChangeListener(this) {
            override fun newProjectCb(newProject: TraceProject<*, *>?) {
                isDisable = (newProject == null)
            }
        }
    } else {
        null
    }

    init {
        setOnAction(onAction)

        val ctx = ViewGroupContextManager.getCurrent()
        if (enableOnlyOnActiveProject && ctx.traceProject == null) this.isDisable = true
        projectChangeListener?.let { ctx.registerProjectChangeListener(it) }
    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        projectChangeListener?.let { ViewGroupContextManager.getCurrent().deregisterProjectChangeListener(it) }
    }
}

private class FileMenu(private val refNode: Node) : Menu(FILE_MENU) {

    companion object {
        private const val FILE_MENU = "File"
        private const val NEW_PROJECT_FROM_TRACES_ACTION = "New Project from Existing Trace(s)..."

        private const val OPEN_PROJECT_ACTION = "Open Project..."
        private const val SAVE_PROJECT_ACTION = "Save Project..."
        private const val CLOSE_PROJECT_ACTION = "Close Current Project"
        private const val EXIT_ACTION = "Exit"
    }

    private val newProjectFromTracesItem = ScopeMenuItem(NEW_PROJECT_FROM_TRACES_ACTION) {
        val project = createNewProjectAction(refNode)
        project?.let { setActiveProject(it) }
    }

    private val openProjectMenuItem = ScopeMenuItem(OPEN_PROJECT_ACTION) { TODO() }
    private val saveProjectMenuItem = ScopeMenuItem(SAVE_PROJECT_ACTION, true) { TODO() }
    private val closeProjectMenuItem = ScopeMenuItem(CLOSE_PROJECT_ACTION, true) { ViewGroupContextManager.getCurrent().switchProject(null) }
    private val exitMenuItem = ScopeMenuItem(EXIT_ACTION) { refNode.scene.window.hide() }

    init {
        items.addAll(newProjectFromTracesItem,
//                NYI
//                openProjectMenuItem,
//                saveProjectMenuItem,
                closeProjectMenuItem,
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
