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
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link TimeRangeTextFields}.
 */
public class TimeRangeTextFieldsTest {

    protected static final KeyEvent ENTER_EVENT = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false);
    protected static final KeyEvent ESCAPE_EVENT = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false);

    protected static final long LIMIT_START = 1000;
    protected static final long LIMIT_END = 2000;

    protected static final long INITIAL_START = 1200;
    protected static final long INITIAL_END = 1800;

    private TimeRangeTextFields fixture;

    @BeforeClass
    public static void classSetup() {
        /* Instantiate JavaFX */
        new JFXPanel();
    }

    @Before
    public void setup() {
        fixture = provideFixture();
        fixture.setTimeRange(TimeRange.of(INITIAL_START, INITIAL_END));
    }

    protected TimeRangeTextFields provideFixture() {
        return new TimeRangeTextFields(TimeRange.of(LIMIT_START, LIMIT_END), null);
    }

    protected final TimeRangeTextFields getFixture() {
        return fixture;
    }

    protected final TextField startField() {
        return fixture.getStartTextField();
    }

    protected final TextField endField() {
        return fixture.getEndTextField();
    }

    protected final TextField durationField() {
        return fixture.getDurationTextField();
    }

    protected final void verifyTimeRange(long expectedStart, long expectedEnd) {
        TimeRange expectedRange = TimeRange.of(expectedStart, expectedEnd);

        assertEquals(expectedRange, fixture.getTimeRange());
        assertEquals(TimestampConversion.tsToString(expectedStart), startField().getText());
        assertEquals(TimestampConversion.tsToString(expectedEnd), endField().getText());
        assertEquals(TimestampConversion.tsToString(expectedRange.getDuration()), durationField().getText());
    }

    @Test
    public void testInitialValues() {
        verifyTimeRange(INITIAL_START, INITIAL_END);
    }

    // ------------------------------------------------------------------------
    // Start text field
    // ------------------------------------------------------------------------

    @Test
    public void testNewStart() {
        startField().setText("1500");
        startField().fireEvent(ENTER_EVENT);
        verifyTimeRange(1500, INITIAL_END);
    }

    @Test
    public void testNewStartSmallerThanLimit() {
        startField().setText("800");
        startField().fireEvent(ENTER_EVENT);
        verifyTimeRange(LIMIT_START, INITIAL_END);
    }

    @Test
    public void testNewStartBiggerThanEnd() {
        startField().setText("1900");
        startField().fireEvent(ENTER_EVENT);
        verifyTimeRange(1900, 1900);
    }

    @Test
    public void testNewStartBiggerThanLimit() {
        startField().setText("2200");
        startField().fireEvent(ENTER_EVENT);
        verifyTimeRange(LIMIT_END, LIMIT_END);
    }

    @Test
    public void testNewStartInvalid() {
        startField().setText("abcd");
        startField().fireEvent(ENTER_EVENT);
        verifyTimeRange(INITIAL_START, INITIAL_END);
    }

    @Test
    public void testNewStartCancelled() {
        startField().setText("1500");
        startField().fireEvent(ESCAPE_EVENT);
        verifyTimeRange(INITIAL_START, INITIAL_END);
    }

    // ------------------------------------------------------------------------
    // End text field
    // ------------------------------------------------------------------------

    @Test
    public void testNewEnd() {
        endField().setText("1600");
        endField().fireEvent(ENTER_EVENT);
        verifyTimeRange(INITIAL_START, 1600);
    }

    @Test
    public void testNewEndSmallerThanLimit() {
        endField().setText("800");
        endField().fireEvent(ENTER_EVENT);
        verifyTimeRange(LIMIT_START, LIMIT_START);
    }

    @Test
    public void testNewEndSmallerThanStart() {
        endField().setText("1100");
        endField().fireEvent(ENTER_EVENT);
        verifyTimeRange(1100, 1100);
    }

    @Test
    public void testNewEndBiggerThanLimit() {
        endField().setText("2200");
        endField().fireEvent(ENTER_EVENT);
        verifyTimeRange(INITIAL_START, LIMIT_END);
    }

    @Test
    public void testNewEndInvalid() {
        endField().setText("abcd");
        endField().fireEvent(ENTER_EVENT);
        verifyTimeRange(INITIAL_START, INITIAL_END);
    }

    @Test
    public void testNewEndCancelled() {
        endField().setText("1500");
        endField().fireEvent(ESCAPE_EVENT);
        verifyTimeRange(INITIAL_START, INITIAL_END);
    }

    // ------------------------------------------------------------------------
    // Duration text field
    // ------------------------------------------------------------------------

    @Test
    public void testNewDuration() {
        durationField().setText("700");
        durationField().fireEvent(ENTER_EVENT);
        verifyTimeRange(INITIAL_START, 1900);
    }

    @Test
    public void testNewDurationClampEnd() {
        durationField().setText("900");
        durationField().fireEvent(ENTER_EVENT);
        verifyTimeRange(1100, LIMIT_END);
    }

    @Test
    public void testNewDurationLargerThanLimits() {
        durationField().setText("1500");
        durationField().fireEvent(ENTER_EVENT);
        verifyTimeRange(LIMIT_START, LIMIT_END);
    }

    @Test
    public void testNewDurationInvalid() {
        durationField().setText("abcd");
        durationField().fireEvent(ENTER_EVENT);
        verifyTimeRange(INITIAL_START, INITIAL_END);
    }

    @Test
    public void testNewDurationCancelled() {
        durationField().setText("500");
        durationField().fireEvent(ESCAPE_EVENT);
        verifyTimeRange(INITIAL_START, INITIAL_END);
    }

}
