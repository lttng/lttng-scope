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
 * Tests for {@link AttributePriorityAggregationRule}.
 *
 * @author Alexandre Montplaisir
 * @see IStateAggregationRule
 */
@SuppressWarnings("nls")
public class AttributePriorityAggregationTest extends AggregationTestBase {

    @Override
    protected IStateAggregationRule createRuleWithParameters(IStateSystemWriter ssb,
            int targetQuark, List<String[]> patterns) {
        return new AttributePriorityAggregationRule(ssb, targetQuark, patterns);
    }

    /**
     * Test the {@link AttributePriorityAggregationRule}. This will build a
     * history like this:
     *
     * <pre>
     * Time-------------- 1    2 3 4  5   6 7    8  9 10 11 12 13 14 15 16 17 18 19
     * CPUs
     *  +-- 0             |    |   |  |   |         |  |  |  |        |  |  |  |  |
     *  |   +-- process   [   U  ][    S    ][      U     ]
     *  |   +-- softirq        [      ]          [     ]        [  ]     [  ]
     *  |   +-- irq                [  ]   [         ]        [        ]        [  ]
     * </pre>
     *
     * The attribute "0" (representing CPU 0) will be formed of the aggregation
     * of the three attributes below it, in the following priority: irq,
     * softirq, process.
     */
    @Test
    void fullTest() {
        IStateSystemWriter ss = getStateSystem();
        assertNotNull(ss);

        /* State values and attributes that will be used */
        StateValue PROCESS_USER = StateValue.newValueInt(0);
        StateValue PROCESS_SYSCALL = StateValue.newValueInt(1);
        StateValue SOFTIRQ_ACTIVE = StateValue.newValueInt(2);
        StateValue IRQ_ACTIVE = StateValue.newValueInt(3);
        StateValue NULL_VALUE = StateValue.nullValue();

        int quark0 = ss.getQuarkAbsoluteAndAdd("CPUs", "0");
        int quarkProcess = ss.getQuarkAbsoluteAndAdd("CPUs", "0", "process");
        int quarkSoftirq = ss.getQuarkAbsoluteAndAdd("CPUs", "0", "softirq");
        int quarkIrq = ss.getQuarkAbsoluteAndAdd("CPUs", "0", "irq");

        /* Create and register the aggregation rule */
        IStateAggregationRule rule = new AttributePriorityAggregationRule(ss, quark0,
                Arrays.asList(
                        new String[] { "CPUs", "0", "irq" },
                        new String[] { "CPUs", "0", "softirq" },
                        new String[] { "CPUs", "0", "process" }
                        ));

        ss.addAggregationRule(rule);

        /* Populate the state system */
        try {
            assertEquals(NULL_VALUE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(10, PROCESS_USER, quarkProcess);
            assertEquals(PROCESS_USER, ss.queryOngoingState(quark0));

            ss.modifyAttribute(20, SOFTIRQ_ACTIVE, quarkSoftirq);
            assertEquals(SOFTIRQ_ACTIVE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(30, PROCESS_SYSCALL, quarkProcess);
            assertEquals(SOFTIRQ_ACTIVE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(40, IRQ_ACTIVE, quarkIrq);
            assertEquals(IRQ_ACTIVE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(50, NULL_VALUE, quarkSoftirq);
            ss.modifyAttribute(50, NULL_VALUE, quarkIrq);
            assertEquals(PROCESS_SYSCALL, ss.queryOngoingState(quark0));

            ss.modifyAttribute(60, IRQ_ACTIVE, quarkIrq);
            assertEquals(IRQ_ACTIVE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(70, PROCESS_USER, quarkProcess);
            assertEquals(IRQ_ACTIVE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(80, SOFTIRQ_ACTIVE, quarkSoftirq);
            assertEquals(IRQ_ACTIVE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(90, NULL_VALUE, quarkIrq);
            assertEquals(SOFTIRQ_ACTIVE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(100, NULL_VALUE, quarkSoftirq);
            assertEquals(PROCESS_USER, ss.queryOngoingState(quark0));

            ss.modifyAttribute(110, NULL_VALUE, quarkProcess);
            assertEquals(NULL_VALUE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(120, IRQ_ACTIVE, quarkIrq);
            assertEquals(IRQ_ACTIVE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(130, SOFTIRQ_ACTIVE, quarkSoftirq);
            assertEquals(IRQ_ACTIVE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(140, NULL_VALUE, quarkSoftirq);
            assertEquals(IRQ_ACTIVE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(150, NULL_VALUE, quarkIrq);
            assertEquals(NULL_VALUE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(160, SOFTIRQ_ACTIVE, quarkSoftirq);
            assertEquals(SOFTIRQ_ACTIVE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(170, NULL_VALUE, quarkSoftirq);
            assertEquals(NULL_VALUE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(180, IRQ_ACTIVE, quarkIrq);
            assertEquals(IRQ_ACTIVE, ss.queryOngoingState(quark0));

            ss.modifyAttribute(190, NULL_VALUE, quarkIrq);
            assertEquals(NULL_VALUE, ss.queryOngoingState(quark0));

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }

        ss.closeHistory(200);

        /* Check the results of queries */
        verifyInterval(  5, quark0,   0,   9, NULL_VALUE);
        verifyInterval( 15, quark0,  10,  19, PROCESS_USER);
        verifyInterval( 25, quark0,  20,  39, SOFTIRQ_ACTIVE);
        verifyInterval( 35, quark0,  20,  39, SOFTIRQ_ACTIVE);
        verifyInterval( 45, quark0,  40,  49, IRQ_ACTIVE);
        verifyInterval( 55, quark0,  50,  59, PROCESS_SYSCALL);
        verifyInterval( 65, quark0,  60,  89, IRQ_ACTIVE);
        verifyInterval( 75, quark0,  60,  89, IRQ_ACTIVE);
        verifyInterval( 85, quark0,  60,  89, IRQ_ACTIVE);
        verifyInterval( 95, quark0,  90,  99, SOFTIRQ_ACTIVE);
        verifyInterval(105, quark0, 100, 109, PROCESS_USER);
        verifyInterval(115, quark0, 110, 119, NULL_VALUE);
        verifyInterval(125, quark0, 120, 149, IRQ_ACTIVE);
        verifyInterval(135, quark0, 120, 149, IRQ_ACTIVE);
        verifyInterval(145, quark0, 120, 149, IRQ_ACTIVE);
        verifyInterval(155, quark0, 150, 159, NULL_VALUE);
        verifyInterval(165, quark0, 160, 169, SOFTIRQ_ACTIVE);
        verifyInterval(175, quark0, 170, 179, NULL_VALUE);
        verifyInterval(185, quark0, 180, 189, IRQ_ACTIVE);
        verifyInterval(195, quark0, 190, 200, NULL_VALUE);
    }
}