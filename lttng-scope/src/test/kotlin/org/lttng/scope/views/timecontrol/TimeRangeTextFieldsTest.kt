/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timecontrol

import com.efficios.jabberwocky.common.TimeRange
import javafx.embed.swing.JFXPanel
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.lttng.scope.application.ScopeOptions

/**
 * Tests for [TimeRangeTextFields].
 */
open class TimeRangeTextFieldsTest {

    companion object {
        val ENTER_EVENT = KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false)
        val ESCAPE_EVENT = KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false)

        const val LIMIT_START = 1000L
        const val LIMIT_END = 2000L

        const val INITIAL_START = 1200L
        const val INITIAL_END = 1800L

        @BeforeClass @JvmStatic
        fun classSetup() {
            /* Instantiate JavaFX */
            JFXPanel()
        }
    }

    protected lateinit var fixture: TimeRangeTextFields
        private set

    @Before
    fun setup() {
        fixture = provideFixture()
        fixture.timeRange = TimeRange.of(INITIAL_START, INITIAL_END)
    }

    protected open fun provideFixture(): TimeRangeTextFields {
        return TimeRangeTextFields(TimeRange.of(LIMIT_START, LIMIT_END), null)
    }

    protected val startField: TextField get() { return fixture.startTextField }
    protected val endField: TextField get() { return fixture.endTextField }
    protected val durationField: TextField get() { return fixture.durationTextField }


    protected fun verifyTimeRange(expectedStart: Long, expectedEnd: Long) {
        val expectedRange = TimeRange.of(expectedStart, expectedEnd)

        assertEquals(expectedRange, fixture.timeRange)
        assertEquals(ScopeOptions.timestampFormat.tsToString(expectedStart), startField.text)
        assertEquals(ScopeOptions.timestampFormat.tsToString(expectedEnd), endField.text)
        assertEquals(ScopeOptions.timestampFormat.tsToString(expectedRange.duration), durationField.text)
    }

    @Test
    fun testInitialValues() {
        verifyTimeRange(INITIAL_START, INITIAL_END)
    }

    // ------------------------------------------------------------------------
    // Start text field
    // ------------------------------------------------------------------------

    @Test
    fun testNewStart() {
        startField.text = "1500"
        startField.fireEvent(ENTER_EVENT)
        verifyTimeRange(1500, INITIAL_END)
    }

    @Test
    fun testNewStartSmallerThanLimit() {
        startField.text = "800"
        startField.fireEvent(ENTER_EVENT)
        verifyTimeRange(LIMIT_START, INITIAL_END)
    }

    @Test
    open fun testNewStartBiggerThanEnd() {
        startField.text = "1900"
        startField.fireEvent(ENTER_EVENT)
        verifyTimeRange(1900, 1900)
    }

    @Test
    open fun testNewStartBiggerThanLimit() {
        startField.text = "2200"
        startField.fireEvent(ENTER_EVENT)
        verifyTimeRange(LIMIT_END, LIMIT_END)
    }

    @Test
    fun testNewStartInvalid() {
        startField.text = "abcd"
        startField.fireEvent(ENTER_EVENT)
        verifyTimeRange(INITIAL_START, INITIAL_END)
    }

    @Test
    fun testNewStartCancelled() {
        startField.text = "1500"
        startField.fireEvent(ESCAPE_EVENT)
        verifyTimeRange(INITIAL_START, INITIAL_END)
    }

    // ------------------------------------------------------------------------
    // End text field
    // ------------------------------------------------------------------------

    @Test
    fun testNewEnd() {
        endField.text = "1600"
        endField.fireEvent(ENTER_EVENT)
        verifyTimeRange(INITIAL_START, 1600)
    }

    @Test
    open fun testNewEndSmallerThanLimit() {
        endField.text = "800"
        endField.fireEvent(ENTER_EVENT)
        verifyTimeRange(LIMIT_START, LIMIT_START)
    }

    @Test
    open fun testNewEndSmallerThanStart() {
        endField.text = "1100"
        endField.fireEvent(ENTER_EVENT)
        verifyTimeRange(1100, 1100)
    }

    @Test
    fun testNewEndBiggerThanLimit() {
        endField.text = "2200"
        endField.fireEvent(ENTER_EVENT)
        verifyTimeRange(INITIAL_START, LIMIT_END)
    }

    @Test
    fun testNewEndInvalid() {
        endField.text = "abcd"
        endField.fireEvent(ENTER_EVENT)
        verifyTimeRange(INITIAL_START, INITIAL_END)
    }

    @Test
    fun testNewEndCancelled() {
        endField.text = "1500"
        endField.fireEvent(ESCAPE_EVENT)
        verifyTimeRange(INITIAL_START, INITIAL_END)
    }

    // ------------------------------------------------------------------------
    // Duration text field
    // ------------------------------------------------------------------------

    @Test
    fun testNewDuration() {
        durationField.text = "700"
        durationField.fireEvent(ENTER_EVENT)
        verifyTimeRange(INITIAL_START, 1900)
    }

    @Test
    fun testNewDurationClampEnd() {
        durationField.text = "900"
        durationField.fireEvent(ENTER_EVENT)
        verifyTimeRange(1100, LIMIT_END)
    }

    @Test
    fun testNewDurationLargerThanLimits() {
        durationField.text = "1500"
        durationField.fireEvent(ENTER_EVENT)
        verifyTimeRange(LIMIT_START, LIMIT_END)
    }

    @Test
    fun testNewDurationInvalid() {
        durationField.text = "abcd"
        durationField.fireEvent(ENTER_EVENT)
        verifyTimeRange(INITIAL_START, INITIAL_END)
    }

    @Test
    fun testNewDurationCancelled() {
        durationField.text = "500"
        durationField.fireEvent(ESCAPE_EVENT)
        verifyTimeRange(INITIAL_START, INITIAL_END)
    }

}
