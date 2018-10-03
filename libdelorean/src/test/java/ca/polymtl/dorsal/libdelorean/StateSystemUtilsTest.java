/*
 * Copyright (C) 2014-2015 École Polytechnique de Montréal
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
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.IntegerStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the {@link StateSystemUtils} class
 *
 * @author Geneviève Bastien
 */
class StateSystemUtilsTest {

    private static final long START_TIME = 1000L;
    private static final @NotNull String DUMMY_STRING = "test"; //$NON-NLS-1$

    private IStateSystemWriter fStateSystem;

    /**
     * Build a small test state system in memory
     */
    @BeforeEach
    void setupStateSystem() {
        try {
            IStateHistoryBackend backend = StateHistoryBackendFactory.createInMemoryBackend(DUMMY_STRING, START_TIME);
            fStateSystem = StateSystemFactory.newStateSystem(backend);
            int quark = fStateSystem.getQuarkAbsoluteAndAdd(DUMMY_STRING);

            fStateSystem.modifyAttribute(1200L, StateValue.newValueInt(10), quark);
            fStateSystem.modifyAttribute(1500L, StateValue.newValueInt(20), quark);
            fStateSystem.closeHistory(2000L);
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Clean-up
     */
    @AfterEach
    void tearDown() {
        fStateSystem.dispose();
    }

    /**
     * Test the {@link StateSystemUtils#queryUntilNonNullValue} method.
     */
    @Test
    void testQueryUntilNonNullValue() {
        IStateSystemReader ss = fStateSystem;
        assertNotNull(ss);

        int quark;
        try {
            quark = ss.getQuarkAbsolute(DUMMY_STRING);

            /* Should return null if requested range is not within range */
            assertNull(StateSystemUtils.queryUntilNonNullValue(ss, quark, 0, 999L));
            assertNull(StateSystemUtils.queryUntilNonNullValue(ss, quark, 2001L, 5000L));

            /*
             * Should return null if request within range, but condition is
             * false
             */
            assertNull(StateSystemUtils.queryUntilNonNullValue(ss, quark, 1000L, 1199L));

            /*
             * Should return the right interval if an interval is within range,
             * even if the range starts or ends outside state system range
             */
            StateInterval interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 1000L, 1300L);
            assertNotNull(interval);
            assertTrue(interval.getStateValue() instanceof IntegerStateValue);
            assertEquals(10, ((IntegerStateValue) interval.getStateValue()).getValue());

            interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 800L, 2500L);
            assertNotNull(interval);
            assertTrue(interval.getStateValue() instanceof IntegerStateValue);
            assertEquals(10, ((IntegerStateValue) interval.getStateValue()).getValue());

            interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 1300L, 1800L);
            assertNotNull(interval);
            assertTrue(interval.getStateValue() instanceof IntegerStateValue);
            assertEquals(10, ((IntegerStateValue) interval.getStateValue()).getValue());

            interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 1500L, 1800L);
            assertNotNull(interval);
            assertTrue(interval.getStateValue() instanceof IntegerStateValue);
            assertEquals(20, ((IntegerStateValue) interval.getStateValue()).getValue());

            interval = StateSystemUtils.queryUntilNonNullValue(ss, quark, 1800L, 2500L);
            assertNotNull(interval);
            assertTrue(interval.getStateValue() instanceof IntegerStateValue);
            assertEquals(20, ((IntegerStateValue) interval.getStateValue()).getValue());

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }

    }

}
