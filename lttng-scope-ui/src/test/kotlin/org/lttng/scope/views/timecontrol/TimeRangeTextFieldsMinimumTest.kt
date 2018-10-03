/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timecontrol

import com.efficios.jabberwocky.common.TimeRange
import org.junit.jupiter.api.Test

/**
 * Tests for [TimeRangeTextFields] specifying a minimum range duration.
 */
class TimeRangeTextFieldsMinimumTest : TimeRangeTextFieldsTest() {

    //    LIMIT_START = 1000
    //    LIMIT_END = 2000
    //    INITIAL_START = 1200
    //    INITIAL_END = 1800

    companion object {
        private const val MINIMUM_DURATION = 100L
    }

    override fun provideFixture() = TimeRangeTextFields(TimeRange.of(LIMIT_START, LIMIT_END), MINIMUM_DURATION)

    // ------------------------------------------------------------------------
    // Start text field
    // ------------------------------------------------------------------------

    @Test
    fun testNewStartMoveEnd() {
        startField.text = "1750"
        startField.fireEvent(ENTER_EVENT)
        verifyTimeRange(1750, 1850)
    }

    @Test
    fun testNewStarMoveBoth() {
        startField.text = "1950"
        startField.fireEvent(ENTER_EVENT)
        verifyTimeRange(1900, LIMIT_END)
    }

    @Test
    override fun testNewStartBiggerThanEnd() {
        startField.text = "1900"
        startField.fireEvent(ENTER_EVENT)
        verifyTimeRange(1900, LIMIT_END)
    }

    @Test
    override fun testNewStartBiggerThanLimit() {
        startField.text = "2200"
        startField.fireEvent(ENTER_EVENT)
        verifyTimeRange(1900, LIMIT_END)
    }

    // ------------------------------------------------------------------------
    // End text field
    // ------------------------------------------------------------------------

    @Test
    fun testNewEndMoveStart() {
        endField.text = "1250"
        endField.fireEvent(ENTER_EVENT)
        verifyTimeRange(1150, 1250)
    }

    @Test
    fun testNewEndMoveBoth() {
        endField.text = "1050"
        endField.fireEvent(ENTER_EVENT)
        verifyTimeRange(LIMIT_START, 1100)
    }

    @Test
    override fun testNewEndSmallerThanLimit() {
        endField.text = "800"
        endField.fireEvent(ENTER_EVENT)
        verifyTimeRange(LIMIT_START, 1100)
    }

    @Test
    override fun testNewEndSmallerThanStart() {
        endField.text = "1150"
        endField.fireEvent(ENTER_EVENT)
        verifyTimeRange(1050, 1150)
    }

    // ------------------------------------------------------------------------
    // Duration text field
    // ------------------------------------------------------------------------

    @Test
    fun testNewDurationTooSmall() {
        durationField.text = "50"
        durationField.fireEvent(ENTER_EVENT)
        verifyTimeRange(INITIAL_START, INITIAL_START + MINIMUM_DURATION)
    }

}
