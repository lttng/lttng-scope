/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application.actions

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.trace.TraceInitializationException
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.layout.Region
import javafx.stage.DirectoryChooser
import org.lttng.scope.ScopePaths
import org.lttng.scope.common.jfx.JfxUtils
import java.nio.file.Files
import java.nio.file.Path

private const val DIRECTORY_CHOOSER_TITLE = "Select CTF Trace Directory to Open"

private const val ERROR_OPENING_ALERT_TITLE = "Error opening trace"
private const val ERROR_OPENING_ALERT_TEXT = "The selected directory does not look like a CTF trace.\n" +
        "Make sure you select the directory with the file named 'metadata'."
private const val ERROR_OPENING_ALERT_WIDTH = 500.0

private var lastUsedDirectory: Path? = ScopePaths.homeDir

/**
 * Ask the user for a directory from which to load a trace. The trace will be returned so it can be added to a project.
 *
 * @param refNode Reference Node, user-facing dialogs should be placed close (at least on the same screen) as this node.
 */
fun askUserForTrace(refNode: Node?): CtfTrace? {
    val tracePath = showTraceSelectionDialog(lastUsedDirectory, refNode) ?: return null
    lastUsedDirectory = tracePath

    return try {
        // TODO This part takes some time, should run in a separate Task
        CtfTrace(tracePath)
    } catch (e: TraceInitializationException) {
        JfxUtils.runLaterAndWait(Runnable {
            with(Alert(Alert.AlertType.ERROR)) {
                title = ERROR_OPENING_ALERT_TITLE
                contentText = ERROR_OPENING_ALERT_TEXT
                with(dialogPane) {
                    minHeight = Region.USE_PREF_SIZE
                    minWidth = ERROR_OPENING_ALERT_WIDTH
                }
                show()
                refNode?.let { JfxUtils.centerDialogOnScreen(this, it) }
            }
        })
        null
    }
}

private fun showTraceSelectionDialog(initialDir: Path?, refNode: Node?): Path? {
    return with(DirectoryChooser()) {
        title = DIRECTORY_CHOOSER_TITLE
        initialDir?.let {
            if (Files.exists(it) && Files.isDirectory(it)) {
                initialDirectory = it.toFile()
            }
        }
        showDialog(refNode?.scene?.window)
    }?.toPath()
}
