/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2012-2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean;

import ca.polymtl.dorsal.libdelorean.backend.IStateHistoryBackend;
import ca.polymtl.dorsal.libdelorean.backend.StateHistoryBackendFactory;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException;
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.IntegerStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for stack-attributes in the Generic State System (using
 * pushAttribute() and popAttribute())
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("nls")
class StateSystemPushPopTest {

    private IStateSystemWriter ss;
    private int attribute;

    private File testHtFile;

    private final static String errMsg = "Caught exception: ";

    /* State values that will be used */
    //private final static ITmfStateValue nullValue = TmfStateValue.nullValue();
    private final static StateValue value1 = StateValue.newValueString("A");
    private final static StateValue value2 = StateValue.newValueInt(10);
    private final static StateValue value3 = StateValue.nullValue();
    private final static StateValue value4 = StateValue.newValueString("D");
    private final static StateValue value5 = StateValue.newValueLong(Long.MAX_VALUE);

    /**
     * Initialization. We run the checks for the return values of
     * .popAttribute() in here, since this is only available when we are
     * building the state history.
     *
     * @throws IOException
     *             If we can write the file to the temporary directory.
     * @throws TimeRangeException
     *             Fails the test
     * @throws AttributeNotFoundException
     *             Fails the test
     */
    @BeforeEach
    void setUp() throws IOException, TimeRangeException,
            AttributeNotFoundException {
        StateValue value;
        testHtFile = File.createTempFile("test", ".ht");

        IStateHistoryBackend backend = StateHistoryBackendFactory.createHistoryTreeBackendNewFile(
                "push-pop-test", requireNonNull(testHtFile), 0, 0);
        ss = new StateSystem(backend, true);

        /* Build the thing */
        final int attrib = ss.getQuarkAbsoluteAndAdd("Test", "stack");

        ss.pushAttribute( 2, value1, attrib);
        ss.pushAttribute( 4, value2, attrib);
        ss.pushAttribute( 6, value3, attrib);
        ss.pushAttribute( 8, value4, attrib);
        ss.pushAttribute(10, value5, attrib);

        value = ss.popAttribute(11, attrib);
        assertEquals(value5, value);

        value = ss.popAttribute(12, attrib);
        assertEquals(value4, value);

        value = ss.popAttribute(14, attrib);
        assertEquals(value3, value);

        value = ss.popAttribute(16, attrib);
        assertEquals(value2, value);

        value = ss.popAttribute(17, attrib);
        assertEquals(value1, value);

        value = ss.popAttribute(20, attrib);
        assertNull(value); // Stack should already be empty here.

        ss.pushAttribute(21, value1, attrib);
        //ss.pushAttribute(22, value1, attrib); //FIXME pushing twice the same value bugs out atm
        ss.pushAttribute(22, value2, attrib);

        value = ss.popAttribute(24, attrib);
        //assertEquals(value1, value);
        assertEquals(value2, value);

        value = ss.popAttribute(26, attrib);
        assertEquals(value1, value);

        value = ss.popAttribute(28, attrib);
        assertNull(value); // Stack should already be empty here.

        ss.closeHistory(30);
        attribute = ss.getQuarkAbsolute("Test", "stack");
    }

    /**
     * Clean-up after running a test. Delete the .ht file we created.
     */
    @AfterEach
    void tearDown() {
        testHtFile.delete();
    }

    /**
     * Test that the value of the stack-attribute at the start and end of the
     * history are correct.
     */
    @Test
    void testBeginEnd() {
        try {
            StateInterval interval = ss.querySingleState(0, attribute);
            assertEquals(0, interval.getStart());
            assertEquals(1, interval.getEnd());
            assertTrue(interval.getStateValue().isNull());

            interval = ss.querySingleState(29, attribute);
            assertEquals(26, interval.getStart());
            assertEquals(30, interval.getEnd());
            assertTrue(interval.getStateValue().isNull());

        } catch (AttributeNotFoundException | TimeRangeException | StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }

    /**
     * Run single queries on the attribute stacks (with .querySingleState()).
     */
    @Test
    void testSingleQueries() {
        try {
            final int subAttribute1 = ss.getQuarkRelative(attribute, "1");
            final int subAttribute2 = ss.getQuarkRelative(attribute, "2");

            /* Test the stack attributes themselves */
            StateInterval interval = ss.querySingleState(11, attribute);
            assertEquals(4, ((IntegerStateValue) interval.getStateValue()).getValue());

            interval = ss.querySingleState(24, attribute);
            assertEquals(1, ((IntegerStateValue) interval.getStateValue()).getValue());

            /* Go retrieve the user values manually */
            interval = ss.querySingleState(10, subAttribute1);
            assertEquals(value1, interval.getStateValue()); //

            interval = ss.querySingleState(22, subAttribute2);
            assertEquals(value2, interval.getStateValue());

            interval = ss.querySingleState(25, subAttribute2);
            assertTrue(interval.getStateValue().isNull()); // Stack depth is 1 at that point.

        } catch (AttributeNotFoundException | TimeRangeException | StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }

    /**
     * Test the .querySingletStackTop() convenience method.
     */
    @Test
    void testStackTop() {
        final IStateSystemWriter ss2 = ss;
        assertNotNull(ss2);

        try {
            StateInterval interval = StateSystemUtils.querySingleStackTop(ss2, 10, attribute);
            assertNotNull(interval);
            assertEquals(value5, interval.getStateValue());

            interval = StateSystemUtils.querySingleStackTop(ss2, 9, attribute);
            assertNotNull(interval);
            assertEquals(value4, interval.getStateValue());

            interval = StateSystemUtils.querySingleStackTop(ss2, 13, attribute);
            assertNotNull(interval);
            assertEquals(value3, interval.getStateValue());

            interval = StateSystemUtils.querySingleStackTop(ss2, 16, attribute);
            assertNotNull(interval);
            assertEquals(value1, interval.getStateValue());

            interval = StateSystemUtils.querySingleStackTop(ss2, 25, attribute);
            assertNotNull(interval);
            assertEquals(value1, interval.getStateValue());

        } catch (AttributeNotFoundException | TimeRangeException | StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }

    /**
     * Test the places where the stack is empty.
     */
    @Test
    void testEmptyStack() {
        final IStateSystemWriter ss2 = ss;
        assertNotNull(ss2);

        try {
            /* At the start */
            StateInterval interval = ss.querySingleState(1, attribute);
            assertTrue(interval.getStateValue().isNull());
            interval = StateSystemUtils.querySingleStackTop(ss2, 1, attribute);
            assertNull(interval);

            /* Between the two "stacks" in the state history */
            interval = ss.querySingleState(19, attribute);
            assertTrue(interval.getStateValue().isNull());
            interval = StateSystemUtils.querySingleStackTop(ss2, 19, attribute);
            assertNull(interval);

            /* At the end */
            interval = ss.querySingleState(27, attribute);
            assertTrue(interval.getStateValue().isNull());
            interval = StateSystemUtils.querySingleStackTop(ss2, 27, attribute);
            assertNull(interval);

        } catch (AttributeNotFoundException | TimeRangeException | StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }

    /**
     * Test full-queries (.queryFullState()) on the attribute stacks.
     */
    @Test
    void testFullQueries() {
        List<StateInterval> state;
        try {
            final int subAttrib1 = ss.getQuarkRelative(attribute, "1");
            final int subAttrib2 = ss.getQuarkRelative(attribute, "2");
            final int subAttrib3 = ss.getQuarkRelative(attribute, "3");
            final int subAttrib4 = ss.getQuarkRelative(attribute, "4");

            /* Stack depth = 5 */
            state = ss.queryFullState(10);
            assertEquals(5, ((IntegerStateValue) state.get(attribute).getStateValue()).getValue());
            assertEquals(value1, state.get(subAttrib1).getStateValue());
            assertEquals(value2, state.get(subAttrib2).getStateValue());
            assertEquals(value3, state.get(subAttrib3).getStateValue());
            assertEquals(value4, state.get(subAttrib4).getStateValue());

            /* Stack is empty */
            state = ss.queryFullState(18);
            assertTrue(state.get(attribute).getStateValue().isNull());
            assertTrue(state.get(subAttrib1).getStateValue().isNull());
            assertTrue(state.get(subAttrib2).getStateValue().isNull());
            assertTrue(state.get(subAttrib3).getStateValue().isNull());
            assertTrue(state.get(subAttrib4).getStateValue().isNull());

            /* Stack depth = 1 */
            state = ss.queryFullState(21);
            assertEquals(1, ((IntegerStateValue) state.get(attribute).getStateValue()).getValue());
            assertEquals(value1, state.get(subAttrib1).getStateValue());
            assertTrue(state.get(subAttrib2).getStateValue().isNull());
            assertTrue(state.get(subAttrib3).getStateValue().isNull());
            assertTrue(state.get(subAttrib4).getStateValue().isNull());

        } catch (AttributeNotFoundException | TimeRangeException | StateSystemDisposedException e) {
            fail(errMsg + e.toString());
        }
    }
}
