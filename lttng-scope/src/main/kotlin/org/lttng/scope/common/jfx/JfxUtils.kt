/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common.jfx

import javafx.application.Platform
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.control.Dialog
import javafx.scene.control.OverrunStyle
import javafx.scene.text.Font
import javafx.stage.Screen
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.util.concurrent.CountDownLatch

/**
 * JavaFX-related utilities
 *
 * @author Alexandre Montplaisir
 */
object JfxUtils {

    /**
     * Double property with a non-modifiable value of 0. For things that should
     * remain at 0.
     */
    @JvmField
    val ZERO_PROPERTY: ReadOnlyDoubleProperty = SimpleDoubleProperty(0.0)

    /**
     * Run the given {@link Runnable} on the UI/main/application thread.
     *
     * If you know for sure you are *not* on the main thread, you should use
     * {@link Platform#runLater} to queue the runnable for the main thread.
     *
     * If you are not sure, you can use this method. The difference with
     * {@link Platform#runLater} is that if you are actually already on the UI
     * thread, the runnable will be run immediately. Whereas calling runLater
     * from the UI will just queue the runnable at the end of the queue.
     *
     * @param r
     *            The runnable to run on the main thread
     */
    @JvmStatic
    fun runOnMainThread(r: Runnable) {
        if (Platform.isFxApplicationThread()) {
            r.run()
        } else {
            Platform.runLater(r)
        }
    }

    @JvmStatic
    fun runLaterAndWait(r: Runnable) {
        if (Platform.isFxApplicationThread()) {
            r.run()
            return
        }

        val latch = CountDownLatch(1)
        Platform.runLater {
            r.run()
            latch.countDown()
        }
        try {
            latch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * Utility method to center a Dialog/Alert on the middle of the current
     * screen. Used to workaround a "bug" with the current version of JavaFX (or
     * the SWT/JavaFX embedding?) where alerts always show on the primary
     * screen, not necessarily the current one.
     *
     * @param dialog
     *            The dialog to reposition. It must be already shown, or else
     *            this will do nothing.
     * @param referenceNode
     *            The dialog should be moved to the same screen as this node's
     *            window.
     */
    @JvmStatic
    fun centerDialogOnScreen(dialog: Dialog<*>, referenceNode: Node) {
        val window = referenceNode.scene?.window ?: return
        val windowRectangle = Rectangle2D(window.x, window.y, window.width, window.height);
        val screen = Screen.getScreensForRectangle(windowRectangle).firstOrNull() ?: Screen.getPrimary()

        dialog.x = (screen.bounds.width - dialog.width) / 2 + screen.bounds.minX
//        dialog.setY((screenBounds.getHeight() - dialog.getHeight()) / 2 + screenBounds.getMinY());
    }
}
