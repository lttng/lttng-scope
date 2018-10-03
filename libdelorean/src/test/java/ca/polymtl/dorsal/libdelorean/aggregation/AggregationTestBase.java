/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.aggregation;

import ca.polymtl.dorsal.libdelorean.IStateSystemReader;
import ca.polymtl.dorsal.libdelorean.IStateSystemWriter;
import ca.polymtl.dorsal.libdelorean.StateSystemFactory;
import ca.polymtl.dorsal.libdelorean.backend.IStateHistoryBackend;
import ca.polymtl.dorsal.libdelorean.backend.StateHistoryBackendFactory;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for aggregation tests.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("nls")
public abstract class AggregationTestBase {

    private @Nullable IStateSystemWriter fStateSystem;

    /**
     * Test setup
     */
    @BeforeEach
    void setup() {
        IStateHistoryBackend backend = StateHistoryBackendFactory.createInMemoryBackend("test-ss", 0);
        fStateSystem = StateSystemFactory.newStateSystem(backend);
    }

    /**
     * Clean-up
     */
    @AfterEach
    void teardown() {
        if (fStateSystem != null) {
            fStateSystem.dispose();
        }
    }

    /**
     * @return The state system test fixture
     */
    protected final IStateSystemWriter getStateSystem() {
        return requireNonNull(fStateSystem);
    }

    /**
     * Verify the contents of an interval. The interval will be obtained and
     * tested both with a single and a full query.
     *
     * @param timestamp
     *            The timestamp of the query
     * @param quark
     *            The target quark of the query
     * @param expectedStartTime
     *            The expected start time of the interval
     * @param expectedEndTime
     *            The expected end time of the interval
     * @param expectedValue
     *            The expected state value
     */
    protected final void verifyInterval(long timestamp, int quark,
            long expectedStartTime,
            long expectedEndTime,
            StateValue expectedValue) {

        IStateSystemReader ss = getStateSystem();
        try {
            StateInterval interval1 = ss.querySingleState(timestamp, quark);
            assertEquals(expectedStartTime, interval1.getStart());
            assertEquals(expectedEndTime, interval1.getEnd());
            assertEquals(quark, interval1.getAttribute());
            assertEquals(expectedValue, interval1.getStateValue());

            StateInterval interval2 = ss.queryFullState(timestamp).get(quark);
            assertEquals(expectedStartTime, interval2.getStart());
            assertEquals(expectedEndTime, interval2.getEnd());
            assertEquals(quark, interval2.getAttribute());
            assertEquals(expectedValue, interval2.getStateValue());

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Create the rule type handle by the test sub-class.
     *
     * @param ssb
     *            Same as the constructor parameter
     * @param targetQuark
     *            Same as the constructor parameter
     * @param patterns
     *            Same as the constructor parameter
     * @return The test rule
     */
    protected abstract IStateAggregationRule createRuleWithParameters(IStateSystemWriter ssb,
            int targetQuark,
            List<String[]> patterns);

    /**
     * Test a rule pointing to only one quark, which does not exist.
     *
     * <pre>
     * quarks
     *         + --target_quark
     *         + --(invalid_quark)
     * </pre>
     *
     * "target_quark" will point to "invalid_quark", which will never be
     * created.
     *
     * It should always return null values, and the interval range should be
     * equal to the full history range. If an aggregate rule does not work this
     * way for some reason, override this test accordingly.
     */
    @Test
    public void testNonExistingQuark() {
        IStateSystemWriter ss = getStateSystem();
        assertNotNull(ss);

        int targetQuark = ss.getQuarkAbsoluteAndAdd("quarks", "target_quark");

        StateValue NULL_VALUE = StateValue.nullValue();

        IStateAggregationRule rule = createRuleWithParameters(ss, targetQuark,
                Collections.singletonList(new String [] { "quarks", "invalid_quark" }));

        ss.addAggregationRule(rule);

        ss.closeHistory(10);

        verifyInterval(5, targetQuark, 0, 10, NULL_VALUE);
    }

    /**
     * Test a rule setup with quark paths, one that exists, another that does
     * not.
     *
     * <pre>
     * quarks
     *         + --target_quark
     *         + --valid_quark
     *         + --(invalid_quark)
     * </pre>
     *
     * "target_quark" will point to both "valid_quark" and "invalid_quark", but
     * the latter will never be created. The aggregation should report only the
     * values of "valid_quark", ignoring the other.
     *
     * If a rule implementation does not aggregate a single valid quark this
     * way, then please override this test accordingly.
     */
    @Test
    public void testExistingAndNonExistingQuarks() {
        IStateSystemWriter ss = getStateSystem();
        assertNotNull(ss);

        int validQuark = ss.getQuarkAbsoluteAndAdd("quarks", "valid_quark");
        int targetQuark = ss.getQuarkAbsoluteAndAdd("quarks", "target_quark");

        StateValue STATE_VALUE = StateValue.newValueInt(1);
        StateValue NULL_VALUE = StateValue.nullValue();

        IStateAggregationRule rule = createRuleWithParameters(ss, targetQuark,
                Arrays.asList(
                        new String[] { "quarks", "valid_quark" },
                        new String[] { "quarks", "invalid_quark" }
                        ));

        ss.addAggregationRule(rule);

        try {
            assertEquals(NULL_VALUE, ss.queryOngoingState(targetQuark));

            ss.modifyAttribute(10, STATE_VALUE, validQuark);

            assertEquals(STATE_VALUE, ss.queryOngoingState(targetQuark));
            verifyInterval(5, targetQuark, 0, 9, NULL_VALUE);

            ss.closeHistory(20);

            verifyInterval(15, targetQuark, 10, 20, STATE_VALUE);

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }
    }

}
