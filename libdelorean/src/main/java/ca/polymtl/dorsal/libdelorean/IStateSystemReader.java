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

import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException;
import ca.polymtl.dorsal.libdelorean.interval.StateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the read-only interface to the generic state system. It contains all
 * the read-only quark-getting methods, as well as the history-querying ones.
 *
 * @author Alexandre Montplaisir
 */
public interface IStateSystemReader extends IStateSystemQuarkResolver {

    /**
     * Return the start time of this history. It usually matches the start time
     * of the original trace.
     *
     * @return The history's registered start time
     */
    long getStartTime();

    /**
     * Return the current end time of the history.
     *
     * @return The current end time of this state history
     */
    long getCurrentEndTime();

    /**
     * While it's possible to query a state history that is being built,
     * sometimes we might want to wait until the construction is finished before
     * we start doing queries.
     *
     * This method blocks the calling thread until the history back-end is done
     * building. If it's already built (ie, opening a pre-existing file) this
     * should return immediately.
     */
    void waitUntilBuilt();

    /**
     * Wait until the state system construction is finished. Similar to
     * {@link #waitUntilBuilt()}, but we also specify a timeout. If the timeout
     * elapses before the construction is finished, the method will return.
     *
     * The return value determines if the return was due to the construction
     * finishing (true), or the timeout elapsing (false).
     *
     * This can be useful, for example, for a component doing queries
     * periodically to the system while it is being built.
     *
     * @param timeout
     *            Timeout value in milliseconds
     * @return True if the return was due to the construction finishing, false
     *         if it was because the timeout elapsed. Same logic as
     *         {@link java.util.concurrent.CountDownLatch#await(long, java.util.concurrent.TimeUnit)}
     */
    boolean waitUntilBuilt(long timeout);

    /**
     * Notify the state system that the trace is being closed, so it should
     * clean up, close its files, etc.
     */
    void dispose();

    // ------------------------------------------------------------------------
    // Query methods
    // ------------------------------------------------------------------------

    /**
     * Returns the current state value we have (in the Transient State) for the
     * given attribute.
     *
     * This is useful even for a StateHistorySystem, as we are guaranteed it
     * will only do a memory access and not go look on disk (and we don't even
     * have to provide a timestamp!)
     *
     * @param attributeQuark
     *            For which attribute we want the current state
     * @return The State value that's "current" for this attribute
     * @throws AttributeNotFoundException
     *             If the requested attribute is invalid
     */
    StateValue queryOngoingState(int attributeQuark)
            throws AttributeNotFoundException;

    /**
     * Get the start time of the current ongoing state, for the specified
     * attribute.
     *
     * @param attribute
     *            Quark of the attribute
     * @return The current start time of the ongoing state
     * @throws AttributeNotFoundException
     *             If the attribute is invalid
     */
    long getOngoingStartTime(int attribute)
            throws AttributeNotFoundException;

    /**
     * Load the complete state information at time 't' into the returned List.
     * You can then get the intervals for single attributes by using
     * List.get(n), where 'n' is the quark of the attribute.
     *
     * On average if you need around 10 or more queries for the same timestamps,
     * use this method. If you need less than 10 (for example, running many
     * queries for the same attributes but at different timestamps), you might
     * be better using the querySingleState() methods instead.
     *
     * @param t
     *            We will recreate the state information to what it was at time
     *            t.
     * @return The List of intervals, where the offset = the quark
     * @throws TimeRangeException
     *             If the 't' parameter is outside of the range of the state
     *             history.
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    List<StateInterval> queryFullState(long t)
            throws StateSystemDisposedException;

    /**
     * Singular query method. This one does not update the whole stateInfo
     * vector, like queryFullState() does. It only searches for one specific
     * entry in the state history.
     *
     * It should be used when you only want very few entries, instead of the
     * whole state (or many entries, but all at different timestamps). If you do
     * request many entries all at the same time, you should use the
     * conventional queryFullState() + List.get() method.
     *
     * @param t
     *            The timestamp at which we want the state
     * @param attributeQuark
     *            Which attribute we want to get the state of
     * @return The StateInterval representing the state
     * @throws TimeRangeException
     *             If 't' is invalid
     * @throws AttributeNotFoundException
     *             If the requested quark does not exist in the model
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    StateInterval querySingleState(long t, int attributeQuark)
            throws AttributeNotFoundException, StateSystemDisposedException;

    /**
     * Query for the specified quarks. Instead of doing several single queries,
     * it is usually faster than doing one call to this method when you want the
     * state for several quarks at the same timestamp.
     *
     * @param t
     *            The timestamp of the query
     * @param quarks
     *            The quarks to query.
     * @return The map of matching intervals, quarks as keys.
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been
     *             disposed.
     */
    Map<Integer, StateInterval> queryStates(long t, Set<Integer> quarks);
}