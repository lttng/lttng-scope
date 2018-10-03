/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2014-2015 École Polytechnique de Montréal
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
import ca.polymtl.dorsal.libdelorean.statevalue.IntegerStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.FutureTask;

/**
 * Provide utility methods for the state system
 *
 * @author Geneviève Bastien
 */
public final class StateSystemUtils {

    private StateSystemUtils() {
    }

    /**
     * Convenience method to query attribute stacks (created with
     * pushAttribute()/popAttribute()). This will return the interval that is
     * currently at the top of the stack, or 'null' if that stack is currently
     * empty. It works similarly to querySingleState().
     *
     * To retrieve the other values in a stack, you can query the sub-attributes
     * manually.
     *
     * @param ss
     *            The state system to query
     * @param t
     *            The timestamp of the query
     * @param stackAttributeQuark
     *            The top-level stack-attribute (that was the target of
     *            pushAttribute() at creation time)
     * @return The interval that was at the top of the stack, or 'null' if the
     *         stack was empty.
     * @throws AttributeNotFoundException
     *             If the attribute was simply not found
     * @throws TimeRangeException
     *             If the given timestamp is invalid
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    public static @Nullable StateInterval querySingleStackTop(IStateSystemReader ss,
                                      long t, int stackAttributeQuark)
            throws AttributeNotFoundException, StateSystemDisposedException {
        StateValue curStackStateValue = ss.querySingleState(t, stackAttributeQuark).getStateValue();

        if (curStackStateValue.isNull()) {
            /* There is nothing stored in this stack at this moment */
            return null;
        }
        int curStackDepth = ((IntegerStateValue) curStackStateValue).getValue();
        if (curStackDepth <= 0) {
            /*
             * This attribute is an integer attribute, but it doesn't seem like
             * it's used as a stack-attribute...
             */
            throw new IllegalArgumentException(ss.getSSID() + " Quark:" + stackAttributeQuark + ", Stack depth:" + curStackDepth);  //$NON-NLS-1$//$NON-NLS-2$
        }

        int subAttribQuark = ss.getQuarkRelative(stackAttributeQuark, String.valueOf(curStackDepth));
        return ss.querySingleState(t, subAttribQuark);
    }

    /**
     * Return a list of state intervals, containing the "history" of a given
     * attribute between timestamps t1 and t2. The list will be ordered by
     * ascending time.
     *
     * Note that contrary to queryFullState(), the returned list here is in the
     * "direction" of time (and not in the direction of attributes, as is the
     * case with queryFullState()).
     *
     * @param ss
     *            The state system to query
     * @param attributeQuark
     *            Which attribute this query is interested in
     * @param t1
     *            Start time of the range query
     * @param t2
     *            Target end time of the query. If t2 is greater than the end of
     *            the trace, we will return what we have up to the end of the
     *            history.
     * @return The List of state intervals that happened between t1 and t2
     * @throws TimeRangeException
     *             If t1 is invalid, or if t2 <= t1
     * @throws AttributeNotFoundException
     *             If the requested quark does not exist in the model.
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    public static List<StateInterval> queryHistoryRange(IStateSystemReader ss,
                                                        int attributeQuark, long t1, long t2)
            throws AttributeNotFoundException, StateSystemDisposedException {

        List<StateInterval> intervals;
        StateInterval currentInterval;
        long ts, tEnd;

        /* Make sure the time range makes sense */
        if (t2 < t1) {
            throw new TimeRangeException(ss.getSSID() + " Start:" + t1 + ", End:" + t2); //$NON-NLS-1$ //$NON-NLS-2$
        }

        /* Set the actual, valid end time of the range query */
        if (t2 > ss.getCurrentEndTime()) {
            tEnd = ss.getCurrentEndTime();
        } else {
            tEnd = t2;
        }

        /* Get the initial state at time T1 */
        intervals = new ArrayList<>();
        currentInterval = ss.querySingleState(t1, attributeQuark);
        intervals.add(currentInterval);

        /* Get the following state changes */
        ts = currentInterval.getEnd();
        while (ts != -1 && ts < tEnd) {
            ts++; /* To "jump over" to the next state in the history */
            currentInterval = ss.querySingleState(ts, attributeQuark);
            intervals.add(currentInterval);
            ts = currentInterval.getEnd();
        }
        return intervals;
    }

    /**
     * Return the state history of a given attribute, but with at most one
     * update per "resolution". This can be useful for populating views (where
     * it's useless to have more than one query per pixel, for example). A
     * progress monitor can be used to cancel the query before completion.
     *
     * @param ss
     *            The state system to query
     * @param attributeQuark
     *            Which attribute this query is interested in
     * @param t1
     *            Start time of the range query
     * @param t2
     *            Target end time of the query. If t2 is greater than the end of
     *            the trace, we will return what we have up to the end of the
     *            history.
     * @param resolution
     *            The "step" of this query
     * @param task
     *            Optional {@link FutureTask} reference that can be used to
     *            indicate if a task calling this method has been cancelled, to
     *            exit early.
     * @return The List of states that happened between t1 and t2
     * @throws TimeRangeException
     *             If t1 is invalid, if t2 <= t1, or if the resolution isn't
     *             greater than zero.
     * @throws AttributeNotFoundException
     *             If the attribute doesn't exist
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    public static List<StateInterval> queryHistoryRange(IStateSystemReader ss,
                                                         int attributeQuark, long t1, long t2, long resolution, @Nullable FutureTask<?> task)
            throws AttributeNotFoundException, StateSystemDisposedException {
        List<StateInterval> intervals = new LinkedList<>();
        StateInterval currentInterval = null;
        long ts, tEnd;

        /* Make sure the time range makes sense */
        if (t2 < t1 || resolution <= 0) {
            throw new TimeRangeException(ss.getSSID() + " Start:" + t1 + ", End:" + t2 + ", Resolution:" + resolution); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /* Set the actual, valid end time of the range query */
        if (t2 > ss.getCurrentEndTime()) {
            tEnd = ss.getCurrentEndTime();
        } else {
            tEnd = t2;
        }

        /*
         * Iterate over the "resolution points". We skip unneeded queries in the
         * case the current interval is longer than the resolution.
         */
        for (ts = t1; ts <= tEnd; ts += ((currentInterval.getEnd() - ts) / resolution + 1) * resolution) {
            if (task != null && task.isCancelled()) {
                return intervals;
            }
            currentInterval = ss.querySingleState(ts, attributeQuark);
            intervals.add(currentInterval);
        }

        /* Add the interval at t2, if it wasn't included already. */
        if (currentInterval != null && currentInterval.getEnd() < tEnd) {
            currentInterval = ss.querySingleState(tEnd, attributeQuark);
            intervals.add(currentInterval);
        }
        return intervals;
    }

    /**
     * Queries intervals in the state system for a given attribute, starting at
     * time t1, until we obtain a non-null value.
     *
     * @param ss
     *            The state system on which to query intervals
     * @param attributeQuark
     *            The attribute quark to query
     * @param t1
     *            Start time of the query
     * @param t2
     *            Time limit of the query. Use {@link Long#MAX_VALUE} for no
     *            limit.
     * @return The first interval from t1 for which the value is not a null
     *         value, or <code>null</code> if no interval was found once we
     *         reach either t2 or the end time of the state system.
     */
    public static @Nullable StateInterval queryUntilNonNullValue(IStateSystemReader ss,
                                                                 int attributeQuark, long t1, long t2) {

        long current = t1;
        /* Make sure the range is ok */
        if (t1 < ss.getStartTime()) {
            current = ss.getStartTime();
        }
        long end = t2;
        if (end < ss.getCurrentEndTime()) {
            end = ss.getCurrentEndTime();
        }
        /* Make sure the time range makes sense */
        if (end < current) {
            return null;
        }

        try {
            while (current < t2) {
                StateInterval currentInterval = ss.querySingleState(current, attributeQuark);
                StateValue value = currentInterval.getStateValue();

                if (!value.isNull()) {
                    return currentInterval;
                }
                current = currentInterval.getEnd() + 1;
            }
        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
            /* Nothing to do */
        }
        return null;
    }

}
