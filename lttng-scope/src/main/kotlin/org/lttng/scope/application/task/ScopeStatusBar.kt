/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application.task

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
import org.controlsfx.control.StatusBar
import org.lttng.scope.application.ScopeWindowManager
import java.util.concurrent.atomic.AtomicInteger

/**
 * Status bar of the main window
 */
class ScopeStatusBar : StatusBar(), ScopeTaskManager.TaskManagerOutput {

    companion object {
        private const val PROGRESS_VIEW_WINDOW_TITLE = "Running Tasks"
        private const val STATUS_BAR_READY_TEXT = "Ready"
    }

    /**
     * The status bar will "own" the progress view, since it is shown by click on the
     * status bar's progress bar.
     */
    private val progressView = ScopeTaskProgressView().apply { ScopeTaskManager.registerOutput(this) }

    private val taskProgressWindow = Stage().apply {
        title = PROGRESS_VIEW_WINDOW_TITLE
        scene = Scene (progressView, 450.0, 450.0)

        ScopeWindowManager.registerWindow(this)
    }


    init {
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
        ScopeTaskManager.registerOutput(this)
    }

    private fun clearRunningTask() {
        Platform.runLater {
            textProperty().unbind()
            progressProperty().unbind()

            text = STATUS_BAR_READY_TEXT
            progress = 0.0
        }
    }

    private fun setRunningTask(task: ScopeTask) {
        Platform.runLater {
            textProperty().bind(task.titleProperty())
            progressProperty().bind(task.progressProperty())
        }
    }

    //--------------------------------------------------------------------------
    // ScopeTaskManager.TaskManagerOutput
    //--------------------------------------------------------------------------

    private val taskCount = AtomicInteger(0)

    override fun taskRegistered(task: ScopeTask) {
        val prevCount = taskCount.getAndIncrement()
        if (prevCount == 0) {
            /* We will show this task on the status bar. */
            setRunningTask(task)
        }
    }

    override fun taskDeregistered(task: ScopeTask) {
        val newCount = taskCount.decrementAndGet()
        if (newCount > 0) {
            val nextTask = ScopeTaskManager.getNextTask()
            if (nextTask != null) {
                setRunningTask(nextTask)
                return
            }
        }
        clearRunningTask()
    }
}
