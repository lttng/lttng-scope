/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.aggregation;

import ca.polymtl.dorsal.libdelorean.IStateSystemWriter;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BitwiseOrAggregationRule}.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("nls")
public class BitwiseOrAggregationTest extends AggregationTestBase {

    @Override
    protected IStateAggregationRule createRuleWithParameters(IStateSystemWriter ssb,
            int targetQuark, List<String[]> patterns) {
        return new BitwiseOrAggregationRule(ssb, targetQuark, patterns);
    }

    /**
     * Test the {@link BitwiseOrAggregationRule}. This will build a history like
     * this:
     *
     * <pre>
     * Time---------  1  2  3  4  5 6 7  8 9 10  11 12  13 14
     * softirq        |  |  |  |  | | |  | |  |   |  |   |  |
     *   +-- raised   [  ]        [   ]  [    ]      [   ]
     *   +-- active         [  ]    [      ]      [         ]
     *
     * </pre>
     *
     * The "softirq" attribute will be formed of the bitwise OR operation on its
     * two sub-attributes "raised" and "active". Each one of those can only have
     * 2 states, yes/no, and will be defined as flags of the same group. This
     * means "softirq" can have 4 possible states.
     */
    @Test
    public void fullTest() {
        IStateSystemWriter ss = getStateSystem();
        assertNotNull(ss);

        /* State values and attributes that will be used */
        int SOFTIRQ_RAISED = (1 << 1);
        int SOFTIRQ_ACTIVE = (1 << 0);

        StateValue NULL_VALUE = StateValue.nullValue();
        StateValue SOFTIRQ_RAISED_VALUE = StateValue.newValueInt(SOFTIRQ_RAISED);
        StateValue SOFTIRQ_ACTIVE_VALUE = StateValue.newValueInt(SOFTIRQ_ACTIVE);

        /* This one will never be inserted directly */
        StateValue SOFTIRQ_RAISED_ACTIVE_VALUE =
                StateValue.newValueInt(SOFTIRQ_ACTIVE | SOFTIRQ_RAISED);

        int quarkSoftirq = ss.getQuarkAbsoluteAndAdd("softirq");
        int quarkRaised = ss.getQuarkAbsoluteAndAdd("softirq", "raised");
        int quarkActive = ss.getQuarkAbsoluteAndAdd("softirq", "active");

        /* Create and register the aggregation rule */
        IStateAggregationRule rule = new BitwiseOrAggregationRule(ss, quarkSoftirq,
                Arrays.asList(
                        new String[] { "softirq", "raised" },
                        new String[] { "softirq", "active" }
                        ));

        ss.addAggregationRule(rule);

        /* Populate the state system */
        try {
            assertEquals(NULL_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(10, SOFTIRQ_RAISED_VALUE, quarkRaised);
            assertEquals(SOFTIRQ_RAISED_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(20, NULL_VALUE, quarkRaised);
            assertEquals(NULL_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(30, SOFTIRQ_ACTIVE_VALUE, quarkActive);
            assertEquals(SOFTIRQ_ACTIVE_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(40, NULL_VALUE, quarkActive);
            assertEquals(NULL_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(50, SOFTIRQ_RAISED_VALUE, quarkRaised);
            assertEquals(SOFTIRQ_RAISED_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(60, SOFTIRQ_ACTIVE_VALUE, quarkActive);
            assertEquals(SOFTIRQ_RAISED_ACTIVE_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(70, NULL_VALUE, quarkRaised);
            assertEquals(SOFTIRQ_ACTIVE_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(80, SOFTIRQ_RAISED_VALUE, quarkRaised);
            assertEquals(SOFTIRQ_RAISED_ACTIVE_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(90, NULL_VALUE, quarkActive);
            assertEquals(SOFTIRQ_RAISED_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(100, NULL_VALUE, quarkRaised);
            assertEquals(NULL_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(110, SOFTIRQ_ACTIVE_VALUE, quarkActive);
            assertEquals(SOFTIRQ_ACTIVE_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(120, SOFTIRQ_RAISED_VALUE, quarkRaised);
            assertEquals(SOFTIRQ_RAISED_ACTIVE_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(130, NULL_VALUE, quarkRaised);
            assertEquals(SOFTIRQ_ACTIVE_VALUE, ss.queryOngoingState(quarkSoftirq));

            ss.modifyAttribute(140, NULL_VALUE, quarkActive);
            assertEquals(NULL_VALUE, ss.queryOngoingState(quarkSoftirq));

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }

        ss.closeHistory(150);

        /* Check the results of queries */
        verifyInterval(  5, quarkSoftirq,   0,   9, NULL_VALUE);
        verifyInterval( 15, quarkSoftirq,  10,  19, SOFTIRQ_RAISED_VALUE);
        verifyInterval( 25, quarkSoftirq,  20,  29, NULL_VALUE);
        verifyInterval( 35, quarkSoftirq,  30,  39, SOFTIRQ_ACTIVE_VALUE);
        verifyInterval( 45, quarkSoftirq,  40,  49, NULL_VALUE);
        verifyInterval( 55, quarkSoftirq,  50,  59, SOFTIRQ_RAISED_VALUE);
        verifyInterval( 65, quarkSoftirq,  60,  69, SOFTIRQ_RAISED_ACTIVE_VALUE);
        verifyInterval( 75, quarkSoftirq,  70,  79, SOFTIRQ_ACTIVE_VALUE);
        verifyInterval( 85, quarkSoftirq,  80,  89, SOFTIRQ_RAISED_ACTIVE_VALUE);
        verifyInterval( 95, quarkSoftirq,  90,  99, SOFTIRQ_RAISED_VALUE);
        verifyInterval(105, quarkSoftirq, 100, 109, NULL_VALUE);
        verifyInterval(115, quarkSoftirq, 110, 119, SOFTIRQ_ACTIVE_VALUE);
        verifyInterval(125, quarkSoftirq, 120, 129, SOFTIRQ_RAISED_ACTIVE_VALUE);
        verifyInterval(135, quarkSoftirq, 130, 139, SOFTIRQ_ACTIVE_VALUE);
        verifyInterval(145, quarkSoftirq, 140, 150, NULL_VALUE);
    }
}