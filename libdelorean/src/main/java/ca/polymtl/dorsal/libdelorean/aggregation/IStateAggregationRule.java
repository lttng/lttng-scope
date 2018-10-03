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
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;

/**
 * Interface for state aggregation rules.
 *
 * An aggregation rule can be "mounted" to a quark in a state system, and will
 * use other values from the same state system to resolve itself, instead of
 * depending on explicit state changes.
 *
 * This can be useful to implement complex stack attributes, where every simple
 * model element can be modelized using regular state changes, but a complex one
 * showing a aggregation of these would be too complicated to modelize directly.
 *
 * A rule can only be mounted to a single quark. To mount similar rules to a set
 * of quarks, create a separate rule object for each one.
 *
 * @author Alexandre Montplaisir
 */
public interface IStateAggregationRule {

    /**
     * Get the state system to which this rule is associated.
     *
     * @return The state system
     */
    IStateSystemWriter getStateSystem();

    /**
     * Get the quark to which this rule is "mounted".
     *
     * @return The target quaark
     */
    int getTargetQuark();

    /**
     * Get the ongoing state of the aggregate quark. This will make use of
     * {@link IStateSystemReader#queryOngoingState}
     *
     * @return The ongoing state of the mounted quark
     */
    StateValue getOngoingAggregatedState();

    /**
     * Get the state of the aggregate quark.
     *
     * @param timestamp
     *            The timestamp of the query
     * @return The corresponding state interval
     */
    StateInterval getAggregatedState(long timestamp);
}