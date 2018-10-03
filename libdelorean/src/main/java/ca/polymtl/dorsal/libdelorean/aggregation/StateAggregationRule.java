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
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Stream;

/**
 * Common implementation for aggregation rules.
 *
 * It provides the state system and target quark fields, which all rules should
 * have.
 *
 * @author Alexandre Montplaisir
 */
public abstract class StateAggregationRule implements IStateAggregationRule {

    private static final String WILDCARD = "*"; //$NON-NLS-1$

    private final IStateSystemWriter fStateSystem;
    private final int fTargetQuark;
    private final List<String[]> fAttributePatterns;

    /**
     * Constructor
     *
     * @param ssb
     *            The state system on which this rule will be associated.
     * @param targetQuark
     *            The aggregate quark where this rule will be "mounted"
     * @param attributePatterns
     *            The paths representing the state system attributes to use in
     *            the resolution of this aggregate state. How exactly they are
     *            used will depend on every implementation of this class.
     */
    protected StateAggregationRule(IStateSystemWriter ssb,
            int targetQuark,
            List<String[]> attributePatterns) {
        fStateSystem = ssb;
        fTargetQuark = targetQuark;

        /* Check that the provided patterns are fine. */
        if (attributePatterns.stream()
                .flatMap(strArray -> Stream.of(strArray))
                .anyMatch(string -> string.equals(WILDCARD))) {
            throw new IllegalArgumentException("Patterns cannot contain wildcards."); //$NON-NLS-1$
        }

        fAttributePatterns = ImmutableList.copyOf(attributePatterns);
    }

    /**
     * Return a fresh Stream over the quarks whose attribute are used for
     * aggregation.
     *
     * Attributes are resolved every time this method is called, because new
     * attributes matching the expected patterns may have been created.
     *
     * @return A fresh stream of the attribute quarks
     */
    protected final Stream<Integer> getQuarkStream() {
        return fAttributePatterns.stream()

                /* Filter out the patterns that do not match existing attributes */
                .map(pattern -> fStateSystem.getQuarks(pattern))
                .filter(quarkList -> !quarkList.isEmpty())

                /*
                 * The patterns should not contain wildcards, there should be
                 * only one element (one quark) in each list.
                 */
                .map(quarkList -> quarkList.get(0));
    }

    @Override
    public final IStateSystemWriter getStateSystem() {
        return fStateSystem;
    }

    @Override
    public final int getTargetQuark() {
        return fTargetQuark;
    }

    @Override
    public abstract StateValue getOngoingAggregatedState();

    @Override
    public abstract StateInterval getAggregatedState(long timestamp);

}
