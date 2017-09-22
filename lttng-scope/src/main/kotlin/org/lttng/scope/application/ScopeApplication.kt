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

        /* Create the application window */
        val root = ScopeMainWindow()

        with (primaryStage) {
            scene = Scene(root)
            title = "LTTng Scope"

            /* Ensure initial window has proper size and subdivisions. */
            width = INITIAL_WINDOW_WIDTH
            setOnShown { root.onShownCB() }

            show()
        }

    }

    override fun stop() {
        System.exit(0)
    }

}