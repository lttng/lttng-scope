/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timecontrol

import com.efficios.jabberwocky.context.ViewGroupContext
import javafx.embed.swing.JFXPanel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.lttng.scope.application.ScopeOptions
import org.lttng.scope.common.TimestampFormat
import org.lttng.scope.common.tests.StubProject
import org.lttng.scope.common.tests.StubTrace

class TimeControlTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun classSetup() {
            /* Instantiate JavaFX */
            JFXPanel()
        }
    }

    private val viewContext = ViewGroupContext()
    private val fixture = TimeControl(viewContext)

    private val projectRangeStartField = fixture.projRangeTextFields[0]
    private val projectRangeEndField = fixture.projRangeTextFields[1]
    private val projectRangeDurationField = fixture.projRangeTextFields[2]

    @Before
    fun setup() {
        val project = StubProject(StubTrace())
        viewContext.switchProject(project.traceProject)
    }

    @Test
    fun testTimestampFormatChange() {
        TimestampFormat.values().forEach {
            ScopeOptions.timestampFormat = it
            when(it) {
                TimestampFormat.YMD_HMS_N -> {
                    assertEquals("1970-01-01 00:00:00.000100000", projectRangeStartField.text)
                    assertEquals("1970-01-01 00:00:00.000200000", projectRangeEndField.text)
                    assertEquals("0.000100000", projectRangeDurationField.text)
                }
                TimestampFormat.HMS_N -> {
                    /* Project range always shows the date in HMS format */
                    assertEquals("1970-01-01 00:00:00.000100000", projectRangeStartField.text)
                    assertEquals("1970-01-01 00:00:00.000200000", projectRangeEndField.text)
                    assertEquals("0.000100000", projectRangeDurationField.text)
                }
                TimestampFormat.SECONDS_POINT_NANOS -> {
                    assertEquals("0.000100000", projectRangeStartField.text)
                    assertEquals("0.000200000", projectRangeEndField.text)
                    assertEquals("0.000100000", projectRangeDurationField.text)
                }
            }
        }
    }
}