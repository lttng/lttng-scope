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
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.IntegerStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Aggregation rule that does bitwise-OR operations of all specified attributes.
 *
 * Will only work with attributes storing Integer state values.
 *
 * @author Alexandre Montplaisir
 */
public class BitwiseOrAggregationRule extends StateAggregationRule {

    /**
     * Constructor
     *
     * Don't forget to also register this rule to the provided state system,
     * using {@link IStateSystemWriter#addAggregationRule}.
     *
     * @param ssb
     *            The state system on which this rule will be associated.
     * @param targetQuark
     *            The aggregate quark where this rule will be "mounted"
     * @param attributePatterns
     *            The attributes (specified with their absolute paths) used to
     *            populate the aggregate. The order of the elements is not
     *            important.
     */
    public BitwiseOrAggregationRule(IStateSystemWriter ssb,
            int targetQuark,
            List<String[]> attributePatterns) {
        super(ssb, targetQuark, attributePatterns);
    }

    @Override
    public StateValue getOngoingAggregatedState() {
        OptionalInt value = getQuarkStream()
                /* Query the value of each quark in the rule */
                .map(quark -> {
                        try {
                            return getStateSystem().queryOngoingState(quark.intValue());
                        } catch (AttributeNotFoundException e) {
                            throw new IllegalStateException("Bad aggregation rule"); //$NON-NLS-1$
                        }
                    })
                .filter(stateValue -> !stateValue.isNull())
                .mapToInt(stateValue -> ((IntegerStateValue) stateValue).getValue())
                .reduce((a, b) -> a | b);

        if (value.isPresent()) {
            return StateValue.newValueInt(value.getAsInt());
        }
        return StateValue.nullValue();
    }

    @Override
    public StateInterval getAggregatedState(long timestamp) {
        IStateSystemWriter ss = getStateSystem();

        /* We first need to get all the valid state intervals */
        Supplier<Stream<StateInterval>> intervals = () -> (getQuarkStream()
                .map(quark -> {
                        try {
                            return ss.querySingleState(timestamp, quark.intValue());
                        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
                            throw new IllegalStateException("Bad aggregation rule"); //$NON-NLS-1$
                        }
                    })
                );

        /* Calculate the value */
        OptionalInt possibleValue = intervals.get()
                .filter(stateInterval -> !stateInterval.getStateValue().isNull())
                .mapToInt(stateInterval -> ((IntegerStateValue) stateInterval.getStateValue()).getValue())
                .reduce((a, b) -> a | b);

        StateValue value = (possibleValue.isPresent() ?
                StateValue.newValueInt(possibleValue.getAsInt()) :
                StateValue.nullValue());

        /* Calculate the dummy interval start (the latest one) */
        long start = intervals.get()
                .mapToLong(StateInterval::getStart)
                .max().orElse(ss.getStartTime());

        /* Calculate the dummy interval end (the earliest one) */
        long end = intervals.get()
                .mapToLong(StateInterval::getEnd)
                .min().orElse(ss.getCurrentEndTime());

        return new StateInterval(start, end, getTargetQuark(), value);
    }

}