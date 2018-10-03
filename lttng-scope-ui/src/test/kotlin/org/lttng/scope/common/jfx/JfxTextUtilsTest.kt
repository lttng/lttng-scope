/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common.jfx

import javafx.scene.control.OverrunStyle
import javafx.scene.text.Font
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JfxTextUtilsTest {

    companion object {
        private const val SHORT_STRING = "short-string"
        private const val LONG_STRING = "long string that will get clipped somewhere"
    }

    @Test
    fun testComputeClippedText() {
        JfxTextUtils.computeClippedText(Font.font(10.0),
                SHORT_STRING,
                100.0,
                OverrunStyle.ELLIPSIS,
                "...").let {
            assertEquals(SHORT_STRING, it)
        }

        JfxTextUtils.computeClippedText(Font.font(10.0),
                LONG_STRING,
                100.0,
                OverrunStyle.ELLIPSIS,
                "...").let {
            assertTrue(it.startsWith("long str"))
            assertTrue(it.endsWith("..."))
        }

        JfxTextUtils.computeClippedText(Font.font(10.0),
                LONG_STRING,
                100.0,
                OverrunStyle.CENTER_ELLIPSIS,
                "...").let {
            assertTrue(it.startsWith("long"))
            assertTrue(it.contains("..."))
            assertFalse(it.endsWith(("...")))
        }
    }

}
