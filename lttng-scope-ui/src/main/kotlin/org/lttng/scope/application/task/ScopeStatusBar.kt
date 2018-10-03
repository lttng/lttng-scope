/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application.task

import com.efficios.jabberwocky.task.JabberwockyTask
import com.efficios.jabberwocky.task.JabberwockyTaskManager
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
import org.controlsfx.control.StatusBar
import org.lttng.scope.application.ScopeMainWindow
import org.lttng.scope.application.ScopeWindowManager

/**
 * Status bar of the main window
 */
class ScopeStatusBar(private val ownerWindow: ScopeMainWindow) : StatusBar(), JabberwockyTaskManager.TaskManagerOutput {

    companion object {
        private const val PROGRESS_VIEW_WINDOW_TITLE = "Running Tasks"
        private const val STATUS_BAR_READY_TEXT = "Ready"
        private const val STATUS_BAR_TOOLTIP = "Click to show/hide all running tasks"
    }

    /**
     * The status bar will "own" the progress view, since it is shown by click on the
     * status bar's progress bar.
     */
    private val progressView = ScopeTaskProgressView().apply {
        JabberwockyTaskManager.registerOutput(this)
    }

    private val taskProgressWindow = Stage().apply {
        title = PROGRESS_VIEW_WINDOW_TITLE
        scene = Scene(progressView, 450.0, 450.0)

        ownerWindow.windowManager.registerWindow(this)
    }


    init {
        tooltip = Tooltip(STATUS_BAR_TOOLTIP)

        /*
         * When shown, the progress bar consumes mouse events, so we'll use
         * addEventFilter() (not addEventHandler()) to make sure they get handled.
         */
        addEventFilter(MouseEvent.MOUSE_CLICKED, EventHandler<MouseEvent> { e ->
            if (e.button != MouseButton.PRIMARY) return@EventHandler

            /* Toggle the display of the progress window */
            with(taskProgressWindow) {
                if (isShowing) {
                    close()
                } else {
                    show()
                }
            }
        })

        clearRunningTask()
        JabberwockyTaskManager.registerOutput(this)
    }

    private fun clearRunningTask() {
        Platform.runLater {
            textProperty().unbind()
            progressProperty().unbind()

            text = STATUS_BAR_READY_TEXT
            progress = 0.0
        }
    }

    private fun setRunningTask(task: JabberwockyTask<*>) {
        Platform.runLater {
            textProperty().bind(task.titleProperty())
            progressProperty().bind(task.progressProperty())
        }
    }

    //--------------------------------------------------------------------------
    // ScopeTaskManager.TaskManagerOutput
    //--------------------------------------------------------------------------

    override fun taskRegistered(task: JabberwockyTask<*>) {
        /* We will show the latest task on the status bar. */
        setRunningTask(task)
    }

    override fun taskDeregistered(task: JabberwockyTask<*>) {
        JabberwockyTaskManager.getLatestTask()
                ?.let { setRunningTask(it) }
                ?: clearRunningTask()
    }
}
