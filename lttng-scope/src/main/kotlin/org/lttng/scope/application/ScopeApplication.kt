/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application

import javafx.application.Application
import javafx.application.Application.launch
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import org.lttng.scope.common.jfx.JfxImageFactory

private const val INITIAL_WINDOW_WIDTH = 1500.0

fun main(args: Array<String>) {
    launch(ScopeApplication::class.java, *args)
}

/**
 * Main application launcher
 */
class ScopeApplication : Application() {

    override fun start(primaryStage: Stage?) {
        primaryStage ?: return
        Platform.setImplicitExit(true)

        /* Do our part in preventing eye cancer. */
        System.setProperty("prism.lcdtext", "false")

        try {
            /* Create the application window */
            val root = ScopeMainWindow()

            with(primaryStage) {
                scene = Scene(root)
                title = "LTTng Scope"

                /* Add the window icons */
                listOf(16, 32, 48, 64, 128, 256)
                        .map { "/appicons/png/scope_icon_${it}x$it.png" }
                        .mapNotNull { JfxImageFactory.getImageFromResource(it) }
                        .let { icons.addAll(it) }

                /* Ensure initial window has proper size and subdivisions. */
                width = INITIAL_WINDOW_WIDTH

                setOnShown { Platform.runLater { root.onShownCB() } }
                setOnHidden { Platform.runLater { root.onHiddenCB() } }

                show()
            }

        } catch (e: Exception) {
            /*
             * Top-level exception handler.
             * Without this, exceptions in the UI don't print their stack trace!
             */
            e.printStackTrace()
        }

    }

    override fun stop() {
        System.exit(0)
    }

}