/*
 * Copyright (C) 2013-2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.backend;

import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException;
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.IntegerStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the in-memory backend
 *
 * @author Matthew Khouzam
 */
class InMemoryBackendTest {

    private static final int NUMBER_OF_ATTRIBUTES = 10;
    private static IStateHistoryBackend fixture;

    /**
     * Test setup. make a state system that is moderately large
     */
    @BeforeAll
    static void init() {
        fixture = StateHistoryBackendFactory.createInMemoryBackend("test-ss", 0); //$NON-NLS-1$
        for (int attribute = 0; attribute < NUMBER_OF_ATTRIBUTES; attribute++) {
            for (int timeStart = 0; timeStart < 1000; timeStart++) {
                try {
                    final int stateEndTime = (timeStart * 100) + 90 + attribute;
                    final int stateStartTime = timeStart * 100 + attribute;
                    fixture.insertPastState(stateStartTime, stateEndTime, attribute, StateValue.newValueInt(timeStart % 100));
                    if (timeStart != 999) {
                        fixture.insertPastState(stateEndTime + 1, stateEndTime + 9, attribute, StateValue.nullValue());
                    }
                } catch (TimeRangeException e) {
                    /* Should not happen here */
                    throw new IllegalStateException();
                }
            }
        }
    }

    private static void testInterval(StateInterval interval, int startTime,
                                     int endTime, int value) {
        assertNotNull(interval);
        assertEquals(startTime, interval.getStart());
        assertEquals(endTime, interval.getEnd());
        int actual = ((IntegerStateValue) interval.getStateValue()).getValue();
        assertEquals(value, actual);
    }


    /**
     * Test at start time
     */
    @Test
    void testStartTime() {
        assertEquals(0, fixture.getStartTime());
    }

    /**
     * Test at end time
     */
    @Test
    void testEndTime() {
        assertEquals(99999, fixture.getEndTime());
    }

    /**
     * Query the state system
     */
    @Test
    void testDoQuery() {
        List<StateInterval> interval = new ArrayList<>(NUMBER_OF_ATTRIBUTES);
        for (int i = 0; i < NUMBER_OF_ATTRIBUTES; i++) {
            interval.add(null);
        }
        try {
            fixture.doQuery(interval, 950);
        } catch (TimeRangeException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }

        assertEquals(NUMBER_OF_ATTRIBUTES, interval.size());
        testInterval(interval.get(0), 900, 990, 9);
        testInterval(interval.get(1), 901, 991, 9);
        testInterval(interval.get(2), 902, 992, 9);
        testInterval(interval.get(3), 903, 993, 9);
        testInterval(interval.get(4), 904, 994, 9);
        testInterval(interval.get(5), 905, 995, 9);
        testInterval(interval.get(6), 906, 996, 9);
        testInterval(interval.get(7), 907, 997, 9);
        testInterval(interval.get(8), 908, 998, 9);
        testInterval(interval.get(9), 909, 999, 9);
    }


    /**
     * Test single attribute then compare it to a full query
     */
    @Test
    void testQueryAttribute() {
        try {
            StateInterval interval[] = new StateInterval[10];
            for (int i = 0; i < 10; i++) {
                interval[i] = fixture.doSingularQuery(950, i);
            }

            testInterval(interval[0], 900, 990, 9);
            testInterval(interval[1], 901, 991, 9);
            testInterval(interval[2], 902, 992, 9);
            testInterval(interval[3], 903, 993, 9);
            testInterval(interval[4], 904, 994, 9);
            testInterval(interval[5], 905, 995, 9);
            testInterval(interval[6], 906, 996, 9);
            testInterval(interval[7], 907, 997, 9);
            testInterval(interval[8], 908, 998, 9);
            testInterval(interval[9], 909, 999, 9);

            List<StateInterval> intervalQuery = new ArrayList<>(NUMBER_OF_ATTRIBUTES);
            for (int i = 0; i < NUMBER_OF_ATTRIBUTES; i++) {
                intervalQuery.add(null);
            }

            fixture.doQuery(intervalQuery, 950);
            StateInterval ref[] = intervalQuery.toArray(new StateInterval[0]);
            assertArrayEquals(ref, interval);

        } catch (TimeRangeException | AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test single attribute that should not exist
     */
    @Test
    void testQueryAttributeEmpty() {
        try {
            StateInterval interval = fixture.doSingularQuery(999, 0);
            assertNotNull(interval);
            assertEquals(StateValue.nullValue(), interval.getStateValue());

        } catch (TimeRangeException | AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test first element in ss
     */
    @Test
    void testBegin() {
        try {
            StateInterval interval = fixture.doSingularQuery(0, 0);
            assertNotNull(interval);
            assertEquals(0, interval.getStart());
            assertEquals(90, interval.getEnd());
            assertEquals(0, ((IntegerStateValue) interval.getStateValue()).getValue());

        } catch (TimeRangeException | AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test last element in ss
     */
    @Test
    void testEnd() {
        try {
            StateInterval interval = fixture.doSingularQuery(99998, 9);
            testInterval(interval, 99909, 99999, 99);

        } catch (TimeRangeException | AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test out of range query
     *
     * @throws TimeRangeException Expected
     */
    @Test
    void testOutOfRange_1() throws TimeRangeException {
        assertThrows(TimeRangeException.class, () -> {
            try {
                StateInterval interval = fixture.doSingularQuery(-1, 0);
                assertNull(interval);

            } catch (AttributeNotFoundException | StateSystemDisposedException e) {
                fail(e.getMessage());
            }
        });
    }

    /**
     * Test out of range query
     *
     * @throws TimeRangeException Expected
     */
    @Test
    void testOutOfRange_2() throws TimeRangeException {
        assertThrows(TimeRangeException.class, () -> {
            try {
                StateInterval interval = fixture.doSingularQuery(100000, 0);
                assertNull(interval);

            } catch (AttributeNotFoundException | StateSystemDisposedException e) {
                fail(e.getMessage());
            }
        });
    }
}
