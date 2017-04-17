/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for the {@link TimeRange} class.
 */
public class TimeRangeTest {

    private static final TimeRange FIXTURE = TimeRange.of(20, 30);

    /**
     * Test that attempting to build a time range with invalid values is forbidden.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadValues() {
        TimeRange.of(20, 10);
    }

    /**
     * Test the {@link TimeRange#getStart()} method.
     */
    @Test
    public void testGetStart() {
        long start = FIXTURE.getStart();
        assertEquals(20, start);
    }

    /**
     * Test the {@link TimeRange#getEnd()} method.
     */
    @Test
    public void testGetEnd() {
        long end = FIXTURE.getEnd();
        assertEquals(30, end);
    }

    /**
     * Test the {@link TimeRange#getDuration()} method.
     */
    @Test
    public void getDuration() {
        long duration = FIXTURE.getDuration();
        assertEquals(10, duration);
    }

    /**
     * Test the {@link TimeRange#contains} method.
     */
    @Test
    public void testContains() {
        assertTrue(FIXTURE.contains(23));
        assertFalse(FIXTURE.contains(10));
        assertFalse(FIXTURE.contains(50));

        /* contains() is inclusive */
        assertTrue(FIXTURE.contains(20));
        assertTrue(FIXTURE.contains(30));
        assertFalse(FIXTURE.contains(19));
        assertFalse(FIXTURE.contains(31));
    }

}
