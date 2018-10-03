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
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Aggregation rule based on attribute priority.
 *
 * At construction, the caller provider a list of attribute paths. These
 * attributes will be used to populate the aggregate attribute. The order of
 * this list is important! The first non-null-value state will be used as value
 * for the aggregate state.
 *
 * If none of the pointed attributes are non-null, only then the reported value
 * will be a null value too.
 *
 * @author Alexandre Montplaisir
 */
public class AttributePriorityAggregationRule extends StateAggregationRule {

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
     *            The list of attributes (specified with their absolute paths)
     *            used to populate the aggregate. The earlier elements in the
     *            list are prioritized over the later ones if several are
     *            non-null.
     */
    public AttributePriorityAggregationRule(IStateSystemWriter ssb,
            int targetQuark,
            List<String[]> attributePatterns) {
        super(ssb, targetQuark, attributePatterns);
    }

    @Override
    public StateValue getOngoingAggregatedState() {
        Optional<StateValue> possibleValue = getQuarkStream()
                /* Query the value of each quark in the rule */
                .map(quark -> {
                    try {
                        return getStateSystem().queryOngoingState(quark.intValue());
                    } catch (AttributeNotFoundException e) {
                        throw new IllegalStateException("Bad aggregation rule"); //$NON-NLS-1$
                    }
                })
                /*
                 * The patterns should have been inserted in order of priority,
                 * the first non-null one is the one we want.
                 */
                .filter(value -> !value.isNull())
                .findFirst();

        return possibleValue.orElse(StateValue.nullValue());
    }

    @Override
    public StateInterval getAggregatedState(long timestamp) {
        IStateSystemWriter ss = getStateSystem();

        /* First we need all the currently valid quarks */
        List<Integer> quarks = getQuarkStream().collect(Collectors.toList());

        /*
         * To determine the value, we will iterate through the corresponding
         * state intervals and keep the first non-null-value one.
         *
         * To determine the start/end times, we need to look through the subset
         * of intervals starting with the one we kept, plus all *higher
         * priority* ones. The lower-priority intervals cannot affect this
         * state, so they are ignored.
         */

        List<StateInterval> intervalsToUse = new ArrayList<>();
        StateValue value = StateValue.nullValue();

        try {
            for (Integer quark : quarks) {
                StateInterval interval = ss.querySingleState(timestamp, quark);
                intervalsToUse.add(interval);

                StateValue sv = interval.getStateValue();
                if (!sv.isNull()) {
                    value = sv;
                    break;
                }
            }

            long start = intervalsToUse.stream()
                    .mapToLong(StateInterval::getStart)
                    .max().orElse(ss.getStartTime());

            long end = intervalsToUse.stream()
                    .mapToLong(StateInterval::getEnd)
                    .min().orElse(ss.getCurrentEndTime());

            return new StateInterval(start, end, getTargetQuark(), value);

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            throw new IllegalStateException("Bad aggregation rule"); //$NON-NLS-1$
        }
    }

}