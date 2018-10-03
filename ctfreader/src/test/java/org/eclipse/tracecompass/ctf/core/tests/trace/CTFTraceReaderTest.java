/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceExtractor;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lttng.scope.ttt.ctf.CtfTestTrace;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class <code>CTFTraceReaderTest</code> contains tests for the class
 * <code>{@link CTFTraceReader}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
class CTFTraceReaderTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;
    private static CtfTestTraceExtractor testTraceWrapper;

    private CTFTraceReader fixture;

    @BeforeAll
    static void setupClass() {
        testTraceWrapper = CtfTestTraceExtractor.extractTestTrace(testTrace);
    }

    @AfterAll
    static void teardownClass() {
        testTraceWrapper.close();
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     */
    @BeforeEach
    void setUp() throws CTFException {
        fixture = new CTFTraceReader(testTraceWrapper.getTrace());
    }

    /**
     * Run the CTFTraceReader(CTFTrace) constructor test. Open a known good
     * trace.
     *
     * @throws CTFException
     */
    @Test
    void testOpen_existing() throws CTFException {
        CTFTrace trace = testTraceWrapper.getTrace();
        try (CTFTraceReader result = new CTFTraceReader(trace);) {
            assertNotNull(result);
        }
    }

    /**
     * Run the CTFTraceReader(CTFTrace) constructor test. Open a non-existing
     * trace, expect the exception.
     *
     * @throws CTFException
     */
    @Test
    void testOpen_nonexisting() {
        assertThrows(CTFException.class, () -> {
            CTFTrace trace = new CTFTrace("badfile.bad");
            try (CTFTraceReader result = new CTFTraceReader(trace);) {
                assertNotNull(result);
            }
        });
    }

    /**
     * Run the CTFTraceReader(CTFTrace) constructor test. Try to pen an invalid
     * path, expect exception.
     *
     * @throws CTFException
     */
    @Test
    void testOpen_invalid() {
        assertThrows(CTFException.class, () -> {
            CTFTrace trace = new CTFTrace("");
            try (CTFTraceReader result = new CTFTraceReader(trace);) {
                assertNotNull(result);
            }
        });
    }

    /**
     * Run the boolean advance() method test. Test advancing normally.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testAdvance_normal() throws CTFException {
        boolean result = fixture.advance();
        assertTrue(result);
    }

    /**
     * Run the boolean advance() method test. Test advancing when we're at the
     * end, so we expect that there is no more events.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testAdvance_end() throws CTFException {
        int i = 0;
        boolean result = fixture.advance();
        while (result) {
            result = fixture.advance();
            i++;
        }
        fixture.seek(0);
        fixture.advance();
        fixture.goToLastEvent();
        i = 1;
        result = fixture.advance();
        while (result) {
            result = fixture.advance();
            i++;
        }
        assertFalse(result);
        assertEquals(i, 1);
    }

    /**
     * Run the CTFTraceReader copy constructor test.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testCopyFrom() throws CTFException {
        try (CTFTraceReader result = fixture.copyFrom();) {
            assertNotNull(result);
        }
    }

    /**
     * Run the getCurrentEventDef() method test. Get the first event's
     * definition.
     */
    @Test
    void testGetCurrentEventDef_first() {
        IEventDefinition result = fixture.getCurrentEventDef();
        assertNotNull(result);
    }

    /**
     * Run the getCurrentEventDef() method test. Get the last event's
     * definition.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testGetCurrentEventDef_last() throws CTFException {
        fixture.goToLastEvent();
        IEventDefinition result = fixture.getCurrentEventDef();
        assertNotNull(result);
    }

    /**
     * Run the long getEndTime() method test.
     */
    @Test
    void testGetEndTime() {
        long result = fixture.getEndTime();
        assertTrue(0L < result);
    }

    /**
     * Run the long getStartTime() method test.
     */
    @Test
    void testGetStartTime() {
        long result = fixture.getStartTime();
        assertTrue(0L < result);
    }

    /**
     * Run the void goToLastEvent() method test.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testGoToLastEvent() throws CTFException {
        fixture.goToLastEvent();
        long ts1 = getTimestamp();
        long ts2 = fixture.getEndTime();
        assertEquals(ts1, ts2);
    }

    /**
     * Run the boolean hasMoreEvents() method test.
     *
     * @throws CTFException
     */
    @Test
    void testHasMoreEvents() {
        boolean result = fixture.hasMoreEvents();
        assertTrue(result);
    }

    /**
     * Run the void printStats() method test with no 'width' parameter.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testPrintStats_noparam() throws CTFException {
        fixture.advance();
        fixture.printStats();
    }

    /**
     * Run the void printStats(int) method test with width = 0.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testPrintStats_width0() throws CTFException {
        fixture.advance();
        fixture.printStats(0);
    }

    /**
     * Run the void printStats(int) method test with width = 1.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testPrintStats_width1() throws CTFException {
        fixture.advance();
        fixture.printStats(1);
    }

    /**
     * Run the void printStats(int) method test with width = 2.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testPrintStats_width2() throws CTFException {
        fixture.advance();
        fixture.printStats(2);
    }

    /**
     * Run the void printStats(int) method test with width = 10.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testPrintStats_width10() throws CTFException {
        fixture.advance();
        fixture.printStats(10);
    }

    /**
     * Run the void printStats(int) method test with width = 100.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testPrintStats_100() throws CTFException {
        for (int i = 0; i < 1000; i++) {
            fixture.advance();
        }
        fixture.printStats(100);
    }

    /**
     * Run the boolean seek(long) method test.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testSeek() throws CTFException {
        long timestamp = 1L;
        boolean result = fixture.seek(timestamp);
        assertTrue(result);
    }

    /**
     * @return
     */
    private long getTimestamp() {
        if (fixture.getCurrentEventDef() != null) {
            return fixture.getTrace().timestampCyclesToNanos(fixture.getCurrentEventDef().getTimestamp());
        }
        return -1;
    }
}
