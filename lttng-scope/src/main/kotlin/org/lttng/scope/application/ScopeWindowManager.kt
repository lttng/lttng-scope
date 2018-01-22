/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application

import javafx.stage.Stage
import java.util.*

/**
 * Manager which will track opened windows (Stages) other than the main application window.
 *
 * Non-modal dialogs or windows created by the application should be registered here. That way,
 * if the user closes the main window, we should ensure the application exits by closing other
 * sub-windows.
 */
object ScopeWindowManager {

    private val trackedWindows: MutableSet<Stage> = Collections.newSetFromMap(WeakHashMap<Stage, Boolean>())

    @Synchronized
    fun registerWindow(window: Stage) {
        trackedWindows.add(window)
    }

    @Synchronized
    fun closeAll() {
        trackedWindows.forEach { it.close() }
        trackedWindows.clear()
    }
}
