/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common.tests

import com.sun.javafx.tk.TKPulseListener
import com.sun.javafx.tk.Toolkit
import java.util.concurrent.CountDownLatch

/**
 * JavaFX-related utility methods for use in tests.
 */
object JfxTestUtils {

    /**
     * Execute all pending UI operations. Since these tests are meant to start
     * on the UI thread, calling this will allow pausing the test and running
     * queued up UI operations.
     */
    @JvmStatic
    fun updateUI() {
        // TODO Replace with Scene.addPostLayoutListener(), etc. in JavaFX 9
        WaitForNextPulseListener().await()
    }

    private class WaitForNextPulseListener : TKPulseListener {

        private val latch = CountDownLatch(2)
        private val tk = Toolkit.getToolkit()

        init {
            tk.addPostSceneTkPulseListener(this)
        }

        override fun pulse() {
            latch.countDown()
            if (latch.count <= 0) {
                tk.removePostSceneTkPulseListener(this)
            }
            tk.requestNextPulse()
        }

        fun await() {
            tk.requestNextPulse()
            try {
                latch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

}
