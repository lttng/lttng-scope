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
import org.junit.Assert.assertEquals
import org.junit.Test

class JfxTextUtilsTest {

    @Test
    fun testComputeClippedText() {
        JfxTextUtils.computeClippedText(Font.font(10.0),
                "short-string",
                100.0,
                OverrunStyle.ELLIPSIS,
                "...").let {
            assertEquals("short-string", it)
        }

        JfxTextUtils.computeClippedText(Font.font(10.0),
                "long string that will get clipped somewhere",
                100.0,
                OverrunStyle.ELLIPSIS,
                "...").let {
            assertEquals("long string that w...", it)
        }

        JfxTextUtils.computeClippedText(Font.font(10.0),
                "long string that will get clipped somewhere",
                100.0,
                OverrunStyle.CENTER_ELLIPSIS,
                "...").let {
            assertEquals("long str...mewhere", it)
        }
    }

}
