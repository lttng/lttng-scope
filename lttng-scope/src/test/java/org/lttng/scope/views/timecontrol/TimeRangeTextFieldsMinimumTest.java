/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timecontrol;

import com.efficios.jabberwocky.common.TimeRange;
import org.junit.Test;

/**
 * Tests for {@link TimeRangeTextFields} specifying a minimum range duration.
 */
public class TimeRangeTextFieldsMinimumTest extends TimeRangeTextFieldsTest {

//    protected static final long LIMIT_START = 1000;
//    protected static final long LIMIT_END = 2000;
//    protected static final long INITIAL_START = 1200;
//    protected static final long INITIAL_END = 1800;

    private static final long MINIMUM_DURATION = 100;

    @Override
    protected TimeRangeTextFields provideFixture() {
        return new TimeRangeTextFields(TimeRange.of(LIMIT_START, LIMIT_END), MINIMUM_DURATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLimits() {
        getFixture().setLimits(TimeRange.of(50, 100));
    }

    // ------------------------------------------------------------------------
    // Start text field
    // ------------------------------------------------------------------------

    @Test
    public void testNewStartMoveEnd() {
        startField().setText("1750");
        startField().fireEvent(ENTER_EVENT);
        verifyTimeRange(1750, 1850);
    }

    @Test
    public void testNewStarMoveBoth() {
        startField().setText("1950");
        startField().fireEvent(ENTER_EVENT);
        verifyTimeRange(1900, LIMIT_END);
    }

    @Override
    @Test
    public void testNewStartBiggerThanEnd() {
        startField().setText("1900");
        startField().fireEvent(ENTER_EVENT);
        verifyTimeRange(1900, LIMIT_END);
    }

    @Override
    @Test
    public void testNewStartBiggerThanLimit() {
        startField().setText("2200");
        startField().fireEvent(ENTER_EVENT);
        verifyTimeRange(1900, LIMIT_END);
    }

    // ------------------------------------------------------------------------
    // End text field
    // ------------------------------------------------------------------------

    @Test
    public void testNewEndMoveStart() {
        endField().setText("1250");
        endField().fireEvent(ENTER_EVENT);
        verifyTimeRange(1150, 1250);
    }

    @Test
    public void testNewEndMoveBoth() {
        endField().setText("1050");
        endField().fireEvent(ENTER_EVENT);
        verifyTimeRange(LIMIT_START, 1100);
    }

    @Override
    @Test
    public void testNewEndSmallerThanLimit() {
        endField().setText("800");
        endField().fireEvent(ENTER_EVENT);
        verifyTimeRange(LIMIT_START, 1100);
    }

    @Override
    @Test
    public void testNewEndSmallerThanStart() {
        endField().setText("1150");
        endField().fireEvent(ENTER_EVENT);
        verifyTimeRange(1050, 1150);
    }

    // ------------------------------------------------------------------------
    // Duration text field
    // ------------------------------------------------------------------------

    @Test
    public void testNewDurationTooSmall() {
        durationField().setText("50");
        durationField().fireEvent(ENTER_EVENT);
        verifyTimeRange(INITIAL_START, INITIAL_START + MINIMUM_DURATION);
    }

}
